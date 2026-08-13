/**
 * @author ArcAnc
 * Created at: 15.03.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.renderer;


import com.arcanc.pulselib.content.mixin.VertexBufferAccessor;
import com.arcanc.pulselib.content.model.deformer.gpu.PGpuDeformerBuffers;
import com.arcanc.pulselib.util.helpers.PLibRenderHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexBuffer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

public class PRenderQueue
{
	private static final Map<RenderStage,
						Map<BatchKey, InstanceBatch>> COMMANDS = new Object2ObjectOpenHashMap<>();
	
	public static void submitBlockEntityMesh(RenderType renderType, VertexBuffer vertexBuffer, InstanceData data)
	{
		submit(RenderStage.SOLID_BLOCKS,
				renderType, vertexBuffer, data);
	}
	
	public static void submitBlockEntityTranslucentMesh(RenderType renderType, VertexBuffer vertexBuffer, InstanceData data)
	{
		submit(RenderStage.TRANSLUCENT_BLOCKS,
				renderType, vertexBuffer, data);
	}
	
	public static void submitItem(ItemDisplayContext context, RenderType renderType, VertexBuffer vertexBuffer, InstanceData data)
	{
		RenderStage stage = switch (context)
		{
			case GUI -> RenderStage.GUI;
			case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND,
			     FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND,
			     HEAD -> RenderStage.ENTITIES;
			case GROUND, FIXED, NONE -> RenderStage.TRANSLUCENT_BLOCKS;
		};
		if (stage!= RenderStage.GUI)
			submit(stage, renderType, vertexBuffer, data);
	}
	
	public static void submitEntityMesh(RenderType renderType, VertexBuffer vertexBuffer, InstanceData data)
	{
		submit(RenderStage.ENTITIES, renderType, vertexBuffer, data);
	}
	
	public static void submit(RenderStage stage,
	                          RenderType type,
	                          VertexBuffer vertexBuffer,
	                          InstanceData data)
	{
		Map<BatchKey, InstanceBatch> stageMap = COMMANDS.computeIfAbsent(stage, s -> new Object2ObjectOpenHashMap<>());
		BatchKey key = new BatchKey(type, vertexBuffer);
		InstanceBatch batch = stageMap.computeIfAbsent(key, k -> new InstanceBatch());
		batch.add(data);
	}
	
	public static void flush(RenderStage stage)
	{
		Map<BatchKey, InstanceBatch> map = COMMANDS.get(stage);
		if (map == null)
			return;
		
		Matrix4f projection = RenderSystem.getProjectionMatrix();
		Matrix4f modelView = RenderSystem.getModelViewMatrix();
		
		for (Map.Entry<BatchKey, InstanceBatch> entry : map.entrySet())
		{
			BatchKey key = entry.getKey();
			InstanceBatch batch = entry.getValue();
			if (batch.size() == 0)
				continue;
			
			RenderType type = key.type();
			VertexBuffer vb = key.buffer();
			
			type.setupRenderState();
			ShaderInstance shader = RenderSystem.getShader();
			if (shader == null)
			{
				type.clearRenderState();
				continue;
			}
			vb.bind();
			
			batch.upload();
			setupInstanceAttributes(batch);
			
			shader.setDefaultUniforms(
					((VertexBufferAccessor)vb).pulselib$getMode(),
					modelView,
					projection,
					PLibRenderHelper.mc().getWindow()
			);
			shader.apply();
			PGpuDeformerBuffers.bind(shader);
			
			GL31.glDrawElementsInstanced(
					((VertexBufferAccessor)vb).pulselib$getMode().asGLMode,
					((VertexBufferAccessor)vb).pulselib$getIndexCount(),
					((VertexBufferAccessor)vb).pulselib$getIndexType().asGLType,
					0,
					batch.size());
			
			disableInstanceAttributes();
			
			batch.clear();
			type.clearRenderState();
		}
	}
	
	public static void cleanup()
	{
		for (Map<BatchKey, InstanceBatch> stageMap : COMMANDS.values())
		{
			for (InstanceBatch batch : stageMap.values())
				batch.delete();
			stageMap.clear();
		}
		COMMANDS.clear();
	}
	
	private static void setupInstanceAttributes(InstanceBatch batch)
	{
		int stride = 16 * 4 + 4 * 4 + 2 * 4 + 2 * 4 + 4 * 4;
		int offset = 0;
		int vbo = batch.instanceVBO;
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		
		for(int q = 0; q < 4; q++)
		{
			GL20.glEnableVertexAttribArray(4 + q);
			GL20.glVertexAttribPointer(4 + q, 4, GL11.GL_FLOAT, false, stride, offset);
			GL33.glVertexAttribDivisor(4 + q,1);
			offset += 16;
		}
		
		GL20.glEnableVertexAttribArray(8);
		GL20.glVertexAttribPointer(8,4, GL11.GL_FLOAT,false, stride, offset);
		GL33.glVertexAttribDivisor(8,1);
		offset += 16;
		
		// Light
		GL20.glEnableVertexAttribArray(9);
		GL20.glVertexAttribPointer(9,2, GL11.GL_FLOAT, false, stride, offset);
		GL33.glVertexAttribDivisor(9,1);
		offset += 8;
		
		// Overlay
		GL20.glEnableVertexAttribArray(10);
		GL20.glVertexAttribPointer(10,2, GL11.GL_FLOAT, false, stride, offset);
		offset += 8;

		GL20.glEnableVertexAttribArray(11);
		GL30.glVertexAttribIPointer(11, 3, GL11.GL_INT, stride, offset);
		GL33.glVertexAttribDivisor(11, 1);
		GL33.glVertexAttribDivisor(10,1);
	}
	
	private static void disableInstanceAttributes()
	{
		for (int q = 4; q <= 11; q++)
		{
			GL20.glDisableVertexAttribArray(q);
			GL33.glVertexAttribDivisor(q,0);
		}
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
	
	public record BatchKey(RenderType type, VertexBuffer buffer) {}
	
	public record InstanceData(
			Matrix4f posMatrix,
			int packedColor,
			int packedLight,
			int packedOverlay,
			int deformerOperationOffset,
			int deformerValueOffset,
			int deformerOperationCount)
	{
		public InstanceData(Matrix4f posMatrix, int packedColor, int packedLight, int packedOverlay)
		{
			this(posMatrix, packedColor, packedLight, packedOverlay, -1, -1, 0);
		}

		public InstanceData(Matrix4f posMatrix, int packedColor, int packedLight, int packedOverlay,
		                    PGpuDeformerBuffers.Submission deformation)
		{
			this(posMatrix, packedColor, packedLight, packedOverlay, deformation.operationOffset(),
					deformation.valueOffset(), deformation.operationCount());
		}
	}
	
	public static class InstanceBatch
	{
		private final List<InstanceData> list = new ObjectArrayList<>();
		private int instanceVBO = -1;
		
		private @Nullable ByteBuffer buffer;
		private int capacity;
		
		private static final int STRIDE = 16 * 4 + 4 * 4 + 2 * 4 + 2 * 4 + 4 * 4;
		
		public void add(InstanceData data)
		{
			this.list.add(data);
		}
		
		public void ensureCapacity(int instances)
		{
			int required = instances * STRIDE;
			
			if(this.buffer == null)
			{
				this.capacity = Math.max(required, 1024);
				this.buffer = MemoryUtil.memAlloc(this.capacity);
				return;
			}
			
			if(required > this.capacity)
			{
				this.capacity = Math.max(required, this.capacity * 2);
				this.buffer = MemoryUtil.memRealloc(this.buffer, this.capacity);
			}
		}
		
		public void upload()
		{
			if(this.instanceVBO == -1)
				this.instanceVBO = GL15.glGenBuffers();
			
			int size = this.list.size();
			
			ensureCapacity(size);
			
			this.buffer.clear();
			
			for (InstanceData q : this.list)
			{
				Matrix4f m = q.posMatrix();
					
				this.buffer.putFloat(m.m00()).putFloat(m.m01()).putFloat(m.m02()).putFloat(m.m03());
				this.buffer.putFloat(m.m10()).putFloat(m.m11()).putFloat(m.m12()).putFloat(m.m13());
				this.buffer.putFloat(m.m20()).putFloat(m.m21()).putFloat(m.m22()).putFloat(m.m23());
				this.buffer.putFloat(m.m30()).putFloat(m.m31()).putFloat(m.m32()).putFloat(m.m33());
				
				this.buffer.putFloat(FastColor.ARGB32.red(q.packedColor()) / 255f);
				this.buffer.putFloat(FastColor.ARGB32.green(q.packedColor()) / 255f);
				this.buffer.putFloat(FastColor.ARGB32.blue(q.packedColor()) / 255f);
				this.buffer.putFloat(FastColor.ARGB32.alpha(q.packedColor()) / 255f);
				
				this.buffer.putFloat(LightTexture.block(q.packedLight()));
				this.buffer.putFloat(LightTexture.sky(q.packedLight()));
				this.buffer.putFloat(q.packedOverlay() & 0xFFFF);
				this.buffer.putFloat((q.packedOverlay() >> 16) & 0xFFFF);
				this.buffer.putInt(q.deformerOperationOffset());
				this.buffer.putInt(q.deformerValueOffset());
				this.buffer.putInt(q.deformerOperationCount());
				this.buffer.putInt(0);
			}
				
			this.buffer.flip();
				
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.instanceVBO);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_DYNAMIC_DRAW);
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
			if (this.instanceVBO != -1)
			{
				GL15.glDeleteBuffers(this.instanceVBO);
				this.instanceVBO = -1;
			}
			
			if(this.buffer != null)
			{
				MemoryUtil.memFree(this.buffer);
				this.buffer = null;
			}
		}
	}
}
