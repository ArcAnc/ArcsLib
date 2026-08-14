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
import com.arcanc.pulselib.content.renderer.gl.PGlMultiDrawExecutor;
import com.arcanc.pulselib.content.renderer.plan.PFrameCompiler;
import com.arcanc.pulselib.content.renderer.plan.PRenderPlan;
import com.arcanc.pulselib.util.PRenderTypes;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;

public class PRenderQueue
{
	private static final PFrameCompiler<RenderStage, RenderType, PBakedMesh, InstanceData> COMPILER = new PFrameCompiler<>(
			Comparator.comparing(RenderType::toString),
			data -> data.posMatrix().m30() * data.posMatrix().m30() +
					data.posMatrix().m31() * data.posMatrix().m31() +
					data.posMatrix().m32() * data.posMatrix().m32());
	private static final PGlMultiDrawExecutor EXECUTOR = new PGlMultiDrawExecutor();
	
	public static void submitBlockEntityMesh(RenderType renderType,
	                                         PBakedMesh mesh,
	                                         @Nullable PMeshDeformation deformation,
	                                         InstanceData data)
	{
		submit(RenderStage.SOLID_BLOCKS,
				renderType, mesh, deformation, data);
	}
	
	public static void submitBlockEntityTranslucentMesh(RenderType renderType,
	                                                    PBakedMesh mesh,
	                                                    @Nullable PMeshDeformation deformation,
	                                                    InstanceData data)
	{
		submit(RenderStage.TRANSLUCENT_BLOCKS, renderType, mesh, deformation, data, true);
	}
	
	public static void submitItem(ItemDisplayContext context,
	                              RenderType renderType,
	                              PBakedMesh mesh,
	                              @Nullable PMeshDeformation deformation,
	                              InstanceData data)
	{
		RenderStage stage = switch (context)
		{
			case GUI -> RenderStage.GUI;
			case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND,
			     FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND,
			     HEAD, ON_SHELF -> RenderStage.ENTITIES;
			case GROUND, FIXED, NONE -> RenderStage.TRANSLUCENT_BLOCKS;
		};
		submit(stage, renderType, mesh, deformation, data, PRenderTypes.isTransparent(renderType));
	}
	
	public static void submitEntityMesh(RenderType renderType,
	                                    PBakedMesh mesh,
	                                    @Nullable PMeshDeformation deformation,
	                                    InstanceData data)
	{
		submit(RenderStage.ENTITIES, renderType, mesh, deformation, data, PRenderTypes.isTransparent(renderType));
	}
	
	public static void submit(RenderStage stage,
	                          RenderType type,
	                          PBakedMesh mesh,
	                          @Nullable PMeshDeformation deformation,
	                          InstanceData data)
	{
		submit(stage, type, mesh, deformation, data, PRenderTypes.isTransparent(type));
	}

	private static void submit(RenderStage stage,
	                           RenderType type,
	                           PBakedMesh mesh,
	                           @Nullable PMeshDeformation deformation,
	                           InstanceData data,
	                           boolean transparent)
	{
		PBakedMesh subdividedMesh = PSubdividedMeshCache.resolve(mesh, deformation == null ? 0 : deformation.subdivisionLevel());
		PGpuDeformerBuffers.Submission deformer = PGpuDeformerBuffers.submit(deformation);
		COMPILER.submit(stage, type, subdividedMesh, data.withDeformer(deformer), transparent);
	}
	
	public static void flush(RenderStage stage)
	{
		PRenderPlan<RenderType, PBakedMesh, InstanceData> plan = COMPILER.compile(stage);
		EXECUTOR.execute(plan);
	}

	public static void cleanUp()
	{
		COMPILER.clear();
		EXECUTOR.cleanup();
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
	
	public record InstanceData(
			Matrix4f posMatrix,
			int packedColor,
			int packedLight,
			int packedOverlay,
			int deformerOperationOffset,
			int deformerValueOffset,
			int deformerOperationCount)
	{
		public InstanceData
		{
			posMatrix = new Matrix4f(posMatrix);
		}

		public InstanceData(Matrix4f posMatrix, int packedColor, int packedLight, int packedOverlay)
		{
			this(posMatrix, packedColor, packedLight, packedOverlay, -1, -1, 0);
		}

		private InstanceData withDeformer(PGpuDeformerBuffers.Submission deformer)
		{
			return new InstanceData(this.posMatrix, this.packedColor, this.packedLight, this.packedOverlay,
					deformer.operationOffset(), deformer.valueOffset(), deformer.operationCount());
		}
	}
	
}
