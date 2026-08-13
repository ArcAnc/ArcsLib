/**
 * @author ArcAnc
 * Created at: 15.03.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.renderer;

import com.arcanc.pulselib.content.renderer.legacy.GlDrawExecutor;
import com.arcanc.pulselib.content.renderer.legacy.GlResourceRegistry;
import com.arcanc.pulselib.content.renderer.legacy.McLegacyGlHostBridge;
import com.arcanc.pulselib.content.renderer.plan.*;
import com.arcanc.pulselib.util.PRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;

public final class PRenderQueue
{
	private static final GlResourceRegistry RESOURCES = new GlResourceRegistry();
	private static final PFrameCompiler<RenderStage> COMPILER = new PFrameCompiler<>();
	private static final GlDrawExecutor EXECUTOR = new GlDrawExecutor(RESOURCES);
	private static final McLegacyGlHostBridge HOST = new McLegacyGlHostBridge();

	private PRenderQueue()
	{
	}

	public static void submitBlockEntityMesh(RenderType renderType, PDynamicGeometry geometry, PInstanceHeader data)
	{
		submit(RenderStage.SOLID_BLOCKS, renderType, geometry, data);
	}

	public static void submitBlockEntityMesh(RenderType renderType, PGeometryData geometry, PInstanceHeader data)
	{
		submit(RenderStage.SOLID_BLOCKS, renderType, geometry, data);
	}

	public static void submitBlockEntityTranslucentMesh(RenderType renderType, PDynamicGeometry geometry, PInstanceHeader data)
	{
		submitTranslucent(RenderStage.TRANSLUCENT_BLOCKS, renderType, geometry, data);
	}

	public static void submitBlockEntityTranslucentMesh(RenderType renderType, PGeometryData geometry, PInstanceHeader data)
	{
		submitTranslucent(RenderStage.TRANSLUCENT_BLOCKS, renderType, geometry, data);
	}

	public static void submitItem(ItemDisplayContext context, RenderType renderType, PDynamicGeometry geometry, PInstanceHeader data)
	{
		RenderStage stage = switch (context)
		{
			case GUI -> RenderStage.GUI;
			case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND,
					 FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND,
					 HEAD -> RenderStage.ENTITIES;
			case GROUND, FIXED, NONE -> RenderStage.TRANSLUCENT_BLOCKS;
		};
		if (stage != RenderStage.GUI)
			submit(stage, renderType, geometry, data);
	}

	public static void submitItem(ItemDisplayContext context, RenderType renderType, PGeometryData geometry, PInstanceHeader data)
	{
		RenderStage stage = stage(context);
		if (stage != RenderStage.GUI)
			submit(stage, renderType, geometry, data, PRenderTypes.isTranslucent(renderType));
	}

	public static void submitEntityMesh(RenderType renderType, PDynamicGeometry geometry, PInstanceHeader data)
	{
		submit(RenderStage.ENTITIES, renderType, geometry, data);
	}

	public static void submitEntityMesh(RenderType renderType, PGeometryData geometry, PInstanceHeader data)
	{
		submit(RenderStage.ENTITIES, renderType, geometry, data);
	}

	public static void submit(RenderStage stage, RenderType type, PDynamicGeometry geometry, PInstanceHeader data)
	{
		submit(stage, type, geometry, data, PRenderTypes.isTranslucent(type));
	}

	public static void submit(RenderStage stage, RenderType type, PGeometryData geometry, PInstanceHeader data)
	{
		submit(stage, type, geometry, data, PRenderTypes.isTranslucent(type));
	}

	private static void submit(RenderStage stage,
	                           RenderType type,
	                           PDynamicGeometry geometry,
	                           PInstanceHeader data,
	                           boolean transparent)
	{
		PPipelineHandle pipeline = RESOURCES.pipeline(type);
		PMeshHandle mesh = RESOURCES.dynamic(geometry);
		int indexCount = RESOURCES.indexCount(geometry);
		COMPILER.submit(stage, pipeline, mesh, RESOURCES.resolveCommand(mesh, indexCount, 1), data, transparent);
	}

	private static void submit(RenderStage stage,
	                           RenderType type,
	                           PGeometryData geometry,
	                           PInstanceHeader data,
	                           boolean transparent)
	{
		PPipelineHandle pipeline = RESOURCES.pipeline(type);
		PMeshHandle mesh = RESOURCES.geometry(geometry);
		COMPILER.submit(stage, pipeline, mesh, RESOURCES.resolveCommand(mesh, geometry.indexCount(), 1), data, transparent);
	}

	private static void submitTranslucent(RenderStage stage, RenderType type, PDynamicGeometry geometry, PInstanceHeader data)
	{
		submit(stage, type, geometry, data, true);
	}

	private static void submitTranslucent(RenderStage stage, RenderType type, PGeometryData geometry, PInstanceHeader data)
	{
		submit(stage, type, geometry, data, true);
	}

	private static RenderStage stage(ItemDisplayContext context)
	{
		return switch (context)
		{
			case GUI -> RenderStage.GUI;
			case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND,
					 FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND,
					 HEAD -> RenderStage.ENTITIES;
			case GROUND, FIXED, NONE -> RenderStage.TRANSLUCENT_BLOCKS;
		};
	}

	public static void flush(RenderStage stage)
	{
		PRenderPlan plan = COMPILER.compile(stage);
		EXECUTOR.execute(plan, HOST.captureFrame());
	}

	public static void cleanup()
	{
		COMPILER.clear();
		EXECUTOR.cleanup();
		RESOURCES.clear();
	}

	public static final class RenderStage
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
		public boolean equals(Object object)
		{
			return object instanceof RenderStage other && this.name.equals(other.name);
		}
	}
}
