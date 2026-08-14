/**
 * @author ArcAnc
 * Created at: 13.08.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.renderer;

import com.arcanc.pulselib.content.model.baked.PBakedMesh;
import com.arcanc.pulselib.content.model.deformer.gpu.PGpuDeformerBuffers;
import com.arcanc.pulselib.content.renderer.plan.PDrawGroup;
import com.arcanc.pulselib.content.renderer.plan.PRenderPlan;
import com.arcanc.pulselib.util.PLibDatabase;
import com.arcanc.pulselib.util.PTextureCache;
import com.arcanc.pulselib.util.helpers.PLibRenderHelper;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.ScissorState;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

/** Executes PulseLib render plans through Minecraft's 26.2 RHI. */
final class RhiDrawExecutor
{
	private static final int INSTANCE_STRIDE = new Std140SizeCalculator().putMat4f().putVec4().putVec2().putVec2().putIVec4().get();
	private static final int MAX_INSTANCES_PER_DRAW = 512;
	private final DeformerBuffers deformerBuffers = new DeformerBuffers();
	private @Nullable MappableRingBuffer instanceBuffer;

	void execute(PRenderPlan plan)
	{
		if (plan.isEmpty())
			return;
		Minecraft minecraft = PLibRenderHelper.mc();
		Matrix4f modelView = RenderSystem.getModelViewMatrixCopy();
		TextureAtlas atlas = PTextureCache.getTextureAtlas();
		GpuTextureView lightTexture = minecraft.gameRenderer.levelLightmap();
		OverlayTexture overlayTexture = minecraft.gameRenderer.overlayTexture();
		DeformerBuffers.Bindings bindings = this.deformerBuffers.upload();
		GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(
				modelView, new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f());
		UploadedInstances instanceData = this.uploadInstances(plan.groups());
		RenderTarget activeTarget = null;
		RenderPass activePass = null;
		try
		{
			for (int groupIndex = 0; groupIndex < plan.groups().size(); groupIndex++)
			{
				PDrawGroup group = plan.groups().get(groupIndex);
				RenderTarget target = group.pipeline().outputTarget().getRenderTarget();
				if (target != activeTarget)
				{
					if (activePass != null)
						activePass.close();
					activeTarget = target;
					activePass = createPass(target, group.mesh());
				}
				draw(activePass, group, dynamicTransforms, instanceData.slice(groupIndex), atlas, lightTexture, overlayTexture, bindings);
			}
		}
		finally
		{
			if (activePass != null)
				activePass.close();
		}
	}

	void cleanup()
	{
		if (this.instanceBuffer != null)
		{
			this.instanceBuffer.close();
			this.instanceBuffer = null;
		}
		this.deformerBuffers.close();
	}

	private UploadedInstances uploadInstances(List<PDrawGroup> groups)
	{
		int size = 0;
		List<Integer> offsets = new ArrayList<>(groups.size());
		for (PDrawGroup group : groups)
		{
			size = alignUniformOffset(size);
			offsets.add(size);
			size += group.instances().size() * INSTANCE_STRIDE;
		}
		size = Math.max(INSTANCE_STRIDE, size);
		if (this.instanceBuffer == null || this.instanceBuffer.size() < size)
		{
			if (this.instanceBuffer != null)
				this.instanceBuffer.close();
			this.instanceBuffer = new MappableRingBuffer(() -> PLibDatabase.rl("instance_data").toLanguageKey(),
					GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, size);
		}
		else
			this.instanceBuffer.rotate();
		try (GpuBufferSlice.MappedView view = this.instanceBuffer.currentBuffer().map(false, true))
		{
			for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++)
			{
				ByteBuffer groupBytes = view.data().duplicate().position(offsets.get(groupIndex));
				Std140Builder builder = Std140Builder.intoBuffer(groupBytes);
				for (PRenderQueue.InstanceData instance : groups.get(groupIndex).instances())
					builder.putMat4f(instance.posMatrix()).
							putVec4(ARGB.red(instance.packedColor()) / 255f, ARGB.green(instance.packedColor()) / 255f,
									ARGB.blue(instance.packedColor()) / 255f, ARGB.alpha(instance.packedColor()) / 255f).
							putVec2(LightCoordsUtil.block(instance.packedLight()), LightCoordsUtil.sky(instance.packedLight())).
							putVec2(instance.packedOverlay() & 0xFFFF, instance.packedOverlay() >> 16 & 0xFFFF).
							putIVec4(instance.deformerOperationOffset(), instance.deformerValueOffset(), instance.deformerOperationCount(), 0);
			}
		}
		return new UploadedInstances(this.instanceBuffer.currentBuffer(), offsets, groups);
	}

	private static int alignUniformOffset(int offset)
	{
		return (offset + 255) & ~255;
	}

	private static RenderPass createPass(RenderTarget target, PBakedMesh mesh)
	{
		GpuTextureView colorTexture = RenderSystem.outputColorTextureOverride != null
				? RenderSystem.outputColorTextureOverride : target.getColorTextureView();
		GpuTextureView depthTexture = target.useDepth
				? (RenderSystem.outputDepthTextureOverride != null ? RenderSystem.outputDepthTextureOverride : target.getDepthTextureView())
				: null;
		return RenderSystem.getDevice().createCommandEncoder().createRenderPass(mesh.uuid()::toString,
				colorTexture, Optional.empty(), depthTexture, OptionalDouble.empty());
	}

	private static void draw(RenderPass pass, PDrawGroup group, GpuBufferSlice dynamicTransforms, GpuBufferSlice instanceData,
						 TextureAtlas atlas, GpuTextureView lightTexture, OverlayTexture overlayTexture, DeformerBuffers.Bindings deformerBuffers)
	{
		if (group.instances().isEmpty())
			return;
		RenderType type = group.pipeline();
		PBakedMesh mesh = group.mesh();
		pass.setPipeline(type.pipeline());
		RenderSystem.bindDefaultUniforms(pass);
		pass.setUniform("DynamicTransforms", dynamicTransforms);
		pass.setUniform("DeformerOperations", deformerBuffers.operations());
		pass.setUniform("DeformerValues", deformerBuffers.values());
		applyActiveScissor(pass);
		pass.bindTexture("Sampler0", atlas.getTextureView(), atlas.getSampler());
		pass.bindTexture("Sampler1", overlayTexture.getTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
		pass.bindTexture("Sampler2", lightTexture, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
		pass.setVertexBuffer(0, mesh.vbo().slice());
		pass.setIndexBuffer(mesh.indices(), mesh.indexType());
		for (int offset = 0; offset < group.instances().size();)
		{
			int count = Math.min(MAX_INSTANCES_PER_DRAW, group.instances().size() - offset);
			pass.setUniform("InstanceData", instanceData.slice((long)offset * INSTANCE_STRIDE, (long)count * INSTANCE_STRIDE));
			pass.drawIndexed(mesh.indicesCount(), count, 0, 0, 0);
			offset += count;
		}
	}

	private record UploadedInstances(GpuBuffer buffer, List<Integer> offsets, List<PDrawGroup> groups)
	{
		private GpuBufferSlice slice(int groupIndex)
		{
			return this.buffer.slice(this.offsets.get(groupIndex), (long)this.groups.get(groupIndex).instances().size() * INSTANCE_STRIDE);
		}
	}

	private static void applyActiveScissor(RenderPass pass)
	{
		ScissorState scissor = RenderSystem.getScissorStateForRenderTypeDraws();
		if (scissor.enabled())
			pass.enableScissor(scissor.x(), scissor.y(), scissor.width(), scissor.height());
		else
			pass.disableScissor();
	}

	private static final class DeformerBuffers
	{
		private static final int MINIMUM_SIZE = Float.BYTES * 4;
		private @Nullable MappableRingBuffer operations, values;

		private Bindings upload()
		{
			this.operations = upload(this.operations, PGpuDeformerBuffers.operations(), PGpuDeformerBuffers.operationsDirty(), "deformer_operations");
			this.values = upload(this.values, PGpuDeformerBuffers.values(), PGpuDeformerBuffers.valuesDirty(), "deformer_values");
			PGpuDeformerBuffers.markOperationsUploaded();
			PGpuDeformerBuffers.markValuesUploaded();
			return new Bindings(this.operations.currentBuffer(), this.values.currentBuffer());
		}

		private void close()
		{
			if (this.operations != null)
				this.operations.close();
			if (this.values != null)
				this.values.close();
			this.operations = this.values = null;
		}

		private static MappableRingBuffer upload(@Nullable MappableRingBuffer buffer, List<Float> data, boolean dirty, String label)
		{
			int size = Math.max(MINIMUM_SIZE, data.size() * Float.BYTES);
			if (buffer == null || buffer.size() < size)
			{
				if (buffer != null)
					buffer.close();
				buffer = new MappableRingBuffer(() -> PLibDatabase.rl(label).toLanguageKey(),
						GpuBuffer.USAGE_UNIFORM_TEXEL_BUFFER | GpuBuffer.USAGE_MAP_WRITE, size);
				dirty = true;
			}
			else if (dirty)
				buffer.rotate();
			if (dirty)
				try (GpuBufferSlice.MappedView mapped = buffer.currentBuffer().map(false, true))
				{
					for (float value : data)
					{
						int bits = Float.floatToRawIntBits(value);
						mapped.data().put((byte)bits).put((byte)(bits >> 8)).put((byte)(bits >> 16)).put((byte)(bits >> 24));
					}
				}
			return buffer;
		}

		private record Bindings(GpuBuffer operations, GpuBuffer values) {}
	}
}
