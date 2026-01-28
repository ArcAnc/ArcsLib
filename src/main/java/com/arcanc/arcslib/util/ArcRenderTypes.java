/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.util;


import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class ArcRenderTypes
{
	public static RenderType trianglesSolid (Identifier texture)
	{
		return RenderTypeProviders.TRIANGLES_SOLID.apply(texture);
	}
	
	public static RenderType trianglesTranslucent(Identifier texture)
	{
		return RenderTypeProviders.TRIANGLES_TRANSLUCENT.apply(texture);
	}
	
	private static class RenderTypeProviders
	{
		public static Function<Identifier, RenderType> TRIANGLES_SOLID = Util.memoize(RenderTypeProviders :: trianglesSolid);
		public static Function<Identifier, RenderType> TRIANGLES_TRANSLUCENT = Util.memoize(RenderTypeProviders :: trianglesTranslucent);
		
		private static @NotNull RenderType trianglesSolid(Identifier loc)
		{
			RenderSetup setup = RenderSetup.builder(RenderPipelinesProvider.TRIANGLES_SOLID).
					withTexture("Sampler0", loc).
					useLightmap().
					useOverlay().
					affectsCrumbling().
					setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE).
					createRenderSetup();
			return RenderType.create("triangles_solid", setup);
		}
		
		private static @NotNull RenderType trianglesTranslucent(Identifier loc)
		{
			RenderSetup setup = RenderSetup.builder(RenderPipelinesProvider.TRIANGLES_TRANSLUCENT).
					withTexture("Sampler0", loc).
					useLightmap().
					useOverlay().
					affectsCrumbling().
					setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE).
					sortOnUpload().
					createRenderSetup();

			return RenderType.create("triangles_translucent", setup);
		}
	}
	
	public static class RenderPipelinesProvider
	{
		private static final Set<RenderPipeline> PIPELINES = new HashSet<>();
		
		private static final RenderPipeline.Snippet TRIANGLES_SNIPPET = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_LIGHT_DIR_SNIPPET).
				withVertexShader("core/entity").
				withFragmentShader("core/entity").
				withSampler("Sampler0").
				withSampler("Sampler2").
				withVertexFormat(DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.TRIANGLES).
				buildSnippet();
		
		public static final RenderPipeline TRIANGLES_SOLID = registerPipeline(RenderPipeline.builder(TRIANGLES_SNIPPET).
				withLocation(Database.rl("pipeline/triangles_cutout_no_cull")).
				withShaderDefine("ALPHA_CUTOUT", 0.1F).
				withShaderDefine("PER_FACE_LIGHTING").
				withSampler("Sampler1").
				withCull(false).
				build());
		
		public static final RenderPipeline TRIANGLES_TRANSLUCENT = registerPipeline(RenderPipeline.builder(TRIANGLES_SNIPPET).
				withLocation(Database.rl("pipeline/triangles_translucent")).
				withShaderDefine("ALPHA_CUTOUT", 0.1F).
				withShaderDefine("PER_FACE_LIGHTING").
				withSampler("Sampler1").
				withBlend(BlendFunction.TRANSLUCENT).
				withCull(false).
				build());
		
		private static RenderPipeline registerPipeline(RenderPipeline pipeline)
		{
			PIPELINES.add(pipeline);
			return pipeline;
		}
		
		private static void registerCustomPipelines(final @NotNull RegisterRenderPipelinesEvent event)
		{
			PIPELINES.forEach(event :: registerPipeline);
			PIPELINES.clear();
		}
	}
	
	public static class VertexFormatProvider
	{
		public static final VertexFormat POSITION_TEX_NORMAL = VertexFormat.builder().
				add("Position", VertexFormatElement.POSITION).
				add("UV0", VertexFormatElement.UV0).
				add("Normal", VertexFormatElement.NORMAL).
				build();
	}
	
	public static void register(@NotNull IEventBus modEventBus)
	{
		modEventBus.addListener(RenderPipelinesProvider :: registerCustomPipelines);
	}
}
