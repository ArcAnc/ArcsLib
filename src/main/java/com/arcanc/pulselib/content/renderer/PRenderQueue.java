/**
 * @author ArcAnc
 * Created at: 01.04.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.renderer;


import com.arcanc.pulselib.content.model.baked.PBakedMesh;
import com.arcanc.pulselib.content.model.baked.PSubdividedMeshCache;
import com.arcanc.pulselib.content.model.deformer.PMeshDeformation;
import com.arcanc.pulselib.content.model.deformer.gpu.PGpuDeformerBuffers;
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
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

public class PRenderQueue
{
	private static final Map<RenderStage,
			Map<BatchKey, InstanceBatch>> COMMANDS = new Object2ObjectOpenHashMap<>();
	private static final DeformerBuffers DEFORMER_BUFFERS = new DeformerBuffers();
	
	public static void submitBlockEntityMesh(RenderType renderType, PBakedMesh mesh, @Nullable PMeshDeformation deformation, InstanceData data)
	{
		submit(RenderStage.SOLID_BLOCKS,
				renderType, mesh, deformation, data);
	}
	
	public static void submitBlockEntityTranslucentMesh(RenderType renderType, PBakedMesh mesh, @Nullable PMeshDeformation deformation, InstanceData data)
	{
		submit(RenderStage.TRANSLUCENT_BLOCKS,
				renderType, mesh, deformation, data);
	}
	
	public static void submitItem(ItemDisplayContext context, RenderType renderType, PBakedMesh mesh, @Nullable PMeshDeformation deformation, InstanceData data)
	{
		RenderStage stage = switch (context)
		{
			case GUI -> RenderStage.GUI;
			case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND,
			     FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND,
			     HEAD, ON_SHELF -> RenderStage.ENTITIES;
			case GROUND, FIXED, NONE -> RenderStage.TRANSLUCENT_BLOCKS;
		};
		submit(stage, renderType, mesh, deformation, data);
	}
	
	public static void submitEntityMesh(RenderType renderType, PBakedMesh mesh, @Nullable PMeshDeformation deformation, InstanceData data)
	{
		submit(RenderStage.ENTITIES, renderType, mesh, deformation, data);
	}
	
	public static void submit(RenderStage stage,
	                          RenderType type,
	                          PBakedMesh mesh, @Nullable PMeshDeformation deformation,
	                          InstanceData data)
	{
		Map<BatchKey, InstanceBatch> stageMap = COMMANDS.computeIfAbsent(stage, s -> new Object2ObjectOpenHashMap<>());
		PBakedMesh subdividedMesh = PSubdividedMeshCache.resolve(mesh, deformation == null ? 0 : deformation.subdivisionLevel());
		BatchKey key = new BatchKey(type, subdividedMesh);
		InstanceBatch batch = stageMap.computeIfAbsent(key, k -> new InstanceBatch());
		batch.add(data.withDeformer(PGpuDeformerBuffers.submit(deformation)));
	}
	
	public static void flush(RenderStage stage)
	{
		Map<BatchKey, InstanceBatch> map = COMMANDS.get(stage);
		if (map == null)
			return;
		Minecraft mc = PLibRenderHelper.mc();
		
		Matrix4f modelView = RenderSystem.getModelViewMatrixCopy();
		TextureAtlas atlas = PTextureCache.getTextureAtlas();
		GpuTextureView lightTexture = mc.gameRenderer.levelLightmap();
		OverlayTexture overlayTexture = mc.gameRenderer.overlayTexture();
		DeformerBuffers.Bindings deformerBuffers = DEFORMER_BUFFERS.upload();
		
		for (Map.Entry<BatchKey, InstanceBatch> entry : map.entrySet())
		{
			BatchKey key = entry.getKey();
			InstanceBatch batch = entry.getValue();
			
			int total = batch.size();
			if (total == 0)
				continue;
			
			RenderType type = key.type();
			PBakedMesh mesh = key.mesh();
			
			GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().
					writeTransform(
							modelView,
							new Vector4f(1.0F, 1.0F, 1.0F, 1.0F),
							new Vector3f(),
							new Matrix4f());
			
			RenderTarget renderTarget = type.outputTarget().getRenderTarget();
			
			GpuTextureView colorTexture = RenderSystem.outputColorTextureOverride != null
					? RenderSystem.outputColorTextureOverride
					: renderTarget.getColorTextureView();
			
			GpuTextureView depthTexture = renderTarget.useDepth
					? (RenderSystem.outputDepthTextureOverride != null ? RenderSystem.outputDepthTextureOverride : renderTarget.getDepthTextureView())
					: null;
			int offset = 0;
			
			while (offset < total)
			{
				int count = Math.min(InstanceBatch.MAX_INSTANCES, total - offset);
			
				MappableRingBuffer instanceData = batch.upload(offset, count);
				
				try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(mesh.uuid() :: toString, colorTexture, Optional.empty(), depthTexture, OptionalDouble.empty()))
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
			
			batch.clear();
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

	public static void cleanUp()
	{
		for (Map<BatchKey, InstanceBatch> stageMap : COMMANDS.values())
		{
			for (InstanceBatch batch : stageMap.values())
				batch.delete();
			stageMap.clear();
		}
		COMMANDS.clear();
		DEFORMER_BUFFERS.close();
		PGpuDeformerBuffers.cleanup();
	}
	
	public static class RenderStage
	{
		public static final RenderStage SOLID_BLOCKS = new RenderStage("solid_blocks");
		public static final RenderStage TRANSLUCENT_BLOCKS = new RenderStage("translucent_blocks");
		public static final RenderStage ENTITIES = new RenderStage("entities");
		public static final RenderStage GUI = new RenderStage("gui");
		
		private final String name;
		
		public RenderStage(String name)
		{
			this.name = name;
		}
		
		@Override
		public int hashCode()
		{
			return this.name.hashCode();
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (obj == null)
				return false;
			if (!(obj instanceof RenderStage other))
				return false;
			if (this == other)
				return true;
			return this.name.equals(other.name);
		}
	}
	
	public record BatchKey(RenderType type, PBakedMesh mesh) {}
	
	public record InstanceData(
			Matrix4f posMatrix,
			int packedColor,
			int packedLight,
			int packedOverlay, int deformerOperationOffset, int deformerValueOffset, int deformerOperationCount)
	{
		public InstanceData(Matrix4f posMatrix, int packedColor, int packedLight, int packedOverlay)
		{
			this(posMatrix, packedColor, packedLight, packedOverlay, -1, -1, 0);
		}
		private InstanceData withDeformer(PGpuDeformerBuffers.Submission deformer)
		{
			return new InstanceData(posMatrix, packedColor, packedLight, packedOverlay, deformer.operationOffset(), deformer.valueOffset(), deformer.operationCount());
		}
	}
	
	public static class InstanceBatch
	{
		private static final int STRIDE = new Std140SizeCalculator().
				putMat4f().
				putVec4().
				putVec2().
				putVec2().
				putIVec4().
				get();
		
		private static final int MAX_INSTANCES = 512;
		
		private final List<InstanceData> list = new ObjectArrayList<>();
		
		private @Nullable MappableRingBuffer instanceData;
		
		public void add(InstanceData data)
		{
			this.list.add(data);
		}
		
		public MappableRingBuffer upload(int offset, int count)
		{
			int bufferSize = count * STRIDE;
			
			if (this.instanceData != null)
				this.instanceData.close();
			
			this.instanceData = new MappableRingBuffer(
					() -> PLibDatabase.rl("instanceData").toLanguageKey(),
					GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE,
					bufferSize);
			
			try (GpuBufferSlice.MappedView instanceDataMappedView = this.instanceData.currentBuffer().map(false, true))
			{
				Std140Builder builder = Std140Builder.intoBuffer(instanceDataMappedView.data());
				for (int q = 0; q < count; q++)
				{
					InstanceData data = this.list.get(offset + q);
					
					builder.putMat4f(data.posMatrix()).
							putVec4(ARGB.red(data.packedColor()) / 255f,
								ARGB.green(data.packedColor()) / 255f,
								ARGB.blue(data.packedColor()) / 255f,
								ARGB.alpha(data.packedColor()) / 255f).
							putVec2(LightCoordsUtil.block(data.packedLight()),
								LightCoordsUtil.sky(data.packedLight())).
							putVec2(data.packedOverlay() & 0xFFFF,
								data.packedOverlay() >> 16 & 0xFFFF).
							putIVec4(data.deformerOperationOffset(), data.deformerValueOffset(), data.deformerOperationCount(), 0);
				}
			}
			return this.instanceData;
		}
		
		public int size()
		{
			return this.list.size();
		}
		
		public void clear()
		{
			this.list.clear();
		}
		
		public void delete()
		{
			if (this.instanceData != null)
			{
				this.instanceData.close();
				this.instanceData = null;
			}
		}
	}

	private static final class DeformerBuffers
	{
		private static final int MINIMUM_SIZE = Float.BYTES * 4;
		private @Nullable MappableRingBuffer operations, values;
		Bindings upload()
		{
			operations = upload(operations, PGpuDeformerBuffers.operations(), PGpuDeformerBuffers.operationsDirty(), "deformer_operations");
			values = upload(values, PGpuDeformerBuffers.values(), PGpuDeformerBuffers.valuesDirty(), "deformer_values");
			PGpuDeformerBuffers.markOperationsUploaded(); PGpuDeformerBuffers.markValuesUploaded();
			return new Bindings(operations.currentBuffer(), values.currentBuffer());
		}
		void close() { if (operations != null) operations.close(); if (values != null) values.close(); operations = values = null; }
		private static MappableRingBuffer upload(@Nullable MappableRingBuffer buffer, List<Float> data, boolean dirty, String label)
		{
			int size = Math.max(MINIMUM_SIZE, data.size() * Float.BYTES);
			if (buffer == null || buffer.size() < size) { if (buffer != null) buffer.close(); buffer = new MappableRingBuffer(() -> PLibDatabase.rl(label).toLanguageKey(), GpuBuffer.USAGE_UNIFORM_TEXEL_BUFFER | GpuBuffer.USAGE_MAP_WRITE, size); dirty = true; }
			else if (dirty) buffer.rotate();
			if (dirty) try (GpuBufferSlice.MappedView mapped = buffer.currentBuffer().map(false, true)) { var bytes = mapped.data(); for (float value : data) { int bits = Float.floatToRawIntBits(value); bytes.put((byte) bits).put((byte) (bits >> 8)).put((byte) (bits >> 16)).put((byte) (bits >> 24)); } }
			return buffer;
		}
		private record Bindings(GpuBuffer operations, GpuBuffer values) {}
	}
}
