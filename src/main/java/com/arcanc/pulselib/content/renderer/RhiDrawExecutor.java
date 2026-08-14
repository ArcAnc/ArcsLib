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
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
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

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

/** Executes PulseLib render plans through Minecraft's 26.2 RHI. */
final class RhiDrawExecutor
{
	private final DeformerBuffers deformerBuffers = new DeformerBuffers();

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
		for (PDrawGroup group : plan.groups())
			draw(group, modelView, atlas, lightTexture, overlayTexture, bindings);
	}

	void cleanup()
	{
		this.deformerBuffers.close();
	}

	private static void draw(PDrawGroup group, Matrix4f modelView, TextureAtlas atlas, GpuTextureView lightTexture, OverlayTexture overlayTexture, DeformerBuffers.Bindings deformerBuffers)
	{
		if (group.instances().isEmpty())
			return;
		InstanceBatch instances = new InstanceBatch(group.instances());
		try
		{
			RenderType type = group.pipeline();
			PBakedMesh mesh = group.mesh();
			GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(
					modelView, new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f());
			RenderTarget renderTarget = type.outputTarget().getRenderTarget();
			GpuTextureView colorTexture = RenderSystem.outputColorTextureOverride != null
					? RenderSystem.outputColorTextureOverride : renderTarget.getColorTextureView();
			GpuTextureView depthTexture = renderTarget.useDepth
					? (RenderSystem.outputDepthTextureOverride != null ? RenderSystem.outputDepthTextureOverride : renderTarget.getDepthTextureView())
					: null;
			for (int offset = 0; offset < instances.size();)
			{
				int count = Math.min(InstanceBatch.MAX_INSTANCES, instances.size() - offset);
				MappableRingBuffer instanceData = instances.upload(offset, count);
				try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(mesh.uuid()::toString, colorTexture, Optional.empty(), depthTexture, OptionalDouble.empty()))
				{
					pass.setPipeline(type.pipeline());
					RenderSystem.bindDefaultUniforms(pass);
					pass.setUniform("DynamicTransforms", dynamicTransforms);
					pass.setUniform("InstanceData", instanceData.currentBuffer());
					pass.setUniform("DeformerOperations", deformerBuffers.operations());
					pass.setUniform("DeformerValues", deformerBuffers.values());
					applyActiveScissor(pass);
					pass.bindTexture("Sampler0", atlas.getTextureView(), atlas.getSampler());
					pass.bindTexture("Sampler1", overlayTexture.getTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
					pass.bindTexture("Sampler2", lightTexture, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
					pass.setVertexBuffer(0, mesh.vbo().slice());
					pass.setIndexBuffer(mesh.indices(), mesh.indexType());
					pass.drawIndexed(mesh.indicesCount(), count, 0, 0, 0);
				}
				offset += count;
			}
		}
		finally
		{
			instances.close();
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

	private static final class InstanceBatch
	{
		private static final int STRIDE = new Std140SizeCalculator().putMat4f().putVec4().putVec2().putVec2().putIVec4().get();
		private static final int MAX_INSTANCES = 512;
		private final List<PRenderQueue.InstanceData> instances;
		private @Nullable MappableRingBuffer buffer;

		private InstanceBatch(List<PRenderQueue.InstanceData> instances)
		{
			this.instances = new ObjectArrayList<>(instances);
		}

		private int size()
		{
			return this.instances.size();
		}

		private MappableRingBuffer upload(int offset, int count)
		{
			if (this.buffer != null)
				this.buffer.close();
			this.buffer = new MappableRingBuffer(
					() -> PLibDatabase.rl("instance_data").toLanguageKey(),
					GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, count * STRIDE);
			try (GpuBufferSlice.MappedView view = this.buffer.currentBuffer().map(false, true))
			{
				Std140Builder builder = Std140Builder.intoBuffer(view.data());
				for (int index = 0; index < count; index++)
				{
					PRenderQueue.InstanceData instance = this.instances.get(offset + index);
					builder.putMat4f(instance.posMatrix()).
							putVec4(ARGB.red(instance.packedColor()) / 255f, ARGB.green(instance.packedColor()) / 255f,
									ARGB.blue(instance.packedColor()) / 255f, ARGB.alpha(instance.packedColor()) / 255f).
							putVec2(LightCoordsUtil.block(instance.packedLight()), LightCoordsUtil.sky(instance.packedLight())).
							putVec2(instance.packedOverlay() & 0xFFFF, instance.packedOverlay() >> 16 & 0xFFFF).
							putIVec4(instance.deformerOperationOffset(), instance.deformerValueOffset(), instance.deformerOperationCount(), 0);
				}
			}
			return this.buffer;
		}

		private void close()
		{
			if (this.buffer != null)
			{
				this.buffer.close();
				this.buffer = null;
			}
		}
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
