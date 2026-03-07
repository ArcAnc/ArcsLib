/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arclib.util;


import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.function.Function;

public class ArcRenderTypes
{
	public static class RenderTypeProvider
	{
		private static final Function<ResourceLocation, RenderType> TRIANGLES_SOLID = Util.memoize(RenderTypeProvider :: trianglesSolid);
		
		public static RenderType trianglesSolid(ResourceLocation location)
		{
			RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder().
							setShaderState(ShadersProvider.StateShard.TRIANGLES).
							setTextureState(new RenderStateShard.TextureStateShard(location, false, false)).
							setTransparencyState(RenderStateShard.NO_TRANSPARENCY).
							setLightmapState(RenderStateShard.NO_LIGHTMAP).
							setOverlayState(RenderStateShard.NO_OVERLAY).
							setCullState(RenderStateShard.NO_CULL).
							createCompositeState(true);
			return RenderType.create(
					"triangles_solid",
					VertexFormatProvider.POSITION_TEX_NORMAL,
					VertexFormat.Mode.TRIANGLES,
					1536,
					false,
					false,
					rendertype$compositestate);
			
		}
	}
	
/*	public static class RenderPipelinesProvider
	{
		private static final Set<RenderPipeline> PIPELINES = new HashSet<>();
		
		/*private static final RenderPipeline.Snippet TRIANGLES_SNIPPET = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_LIGHT_DIR_SNIPPET).
				withVertexShader(Database.rl("core/triangles")).
				withFragmentShader(Database.rl("core/triangles")).
				withSampler("Sampler0").
				withSampler("Sampler2").
				withUniform("ColorLightValues", UniformType.UNIFORM_BUFFER).
				withVertexFormat(VertexFormatProvider.POSITION_TEX_NORMAL, VertexFormat.Mode.TRIANGLES).
				buildSnippet();
		
		public static final RenderPipeline TRIANGLES_SOLID = registerPipeline(RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET).
				withFragmentShader(Database.rl("core/triangles")).
				withVertexShader(Database.rl("core/triangles")).
				withLocation(Database.rl("pipeline/triangles_cutout_no_cull")).
				withUniform("ColorLightOverlay", UniformType.UNIFORM_BUFFER).
				withShaderDefine("ALPHA_CUTOUT", 0.1F).
				withSampler("Sampler0").
				withSampler("Sampler1").
				withSampler("Sampler2").
				withCull(false).
				withVertexFormat(VertexFormatProvider.POSITION_TEX_NORMAL, VertexFormat.Mode.TRIANGLES).
				build());
		
		/*public static final RenderPipeline TRIANGLES_TRANSLUCENT = registerPipeline(RenderPipeline.builder(TRIANGLES_SNIPPET).
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
		
		private static void registerCustomPipelines(final RegisterRenderPipelinesEvent event)
		{
			PIPELINES.forEach(event :: registerPipeline);
			PIPELINES.clear();
		}
	}*/
	
	public static class VertexFormatProvider
	{
		public static final VertexFormat POSITION_TEX_NORMAL = VertexFormat.builder().
				add("Position", VertexFormatElement.POSITION).
				add("UV0", VertexFormatElement.UV0).
				add("Normal", VertexFormatElement.NORMAL).
				padding(1).
				build();
	}
	
	public static class ShadersProvider
	{
		public static class StateShard
		{
			private static final RenderStateShard.ShaderStateShard TRIANGLES = new RenderStateShard.ShaderStateShard(() -> TRIANGLES_SHADER);
		}
		
		@Nullable
		public static ShaderInstance TRIANGLES_SHADER;
		
		public static @Nullable ShaderInstance trianglesShader()
		{
			return TRIANGLES_SHADER;
		}
		
		private static void registerShaders(final RegisterShadersEvent event)
		{
			try
			{
				event.registerShader(new ShaderInstance(
								event.getResourceProvider(),
								Database.rl("triangles"),
								VertexFormatProvider.POSITION_TEX_NORMAL),
						shaderInstance -> TRIANGLES_SHADER = shaderInstance);
			}
			catch (IOException e)
			{
				Database.LOGGER.warn("Failed to register shaders: {}", String.valueOf(e));
			}
		}
	}
	
	public static void register(IEventBus modEventBus)
	{
		//modEventBus.addListener(RenderPipelinesProvider :: registerCustomPipelines);
		modEventBus.addListener(ShadersProvider :: registerShaders);
	}
}
