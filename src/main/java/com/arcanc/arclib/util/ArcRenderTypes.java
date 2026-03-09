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
		private static final Function<ResourceLocation, RenderType> TRIANGLES_CUTOUT = Util.memoize(RenderTypeProvider :: trianglesCutout);
		private static final Function<ResourceLocation, RenderType> TRIANGLES_TRANSLUCENT = Util.memoize(RenderTypeProvider :: trianglesTranslucent);
		
		public static RenderType trianglesSolid(ResourceLocation location)
		{
			RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder().
							setShaderState(ShadersProvider.StateShard.TRIANGLES_SOLID_STATE_SHARD).
							setTextureState(new RenderStateShard.TextureStateShard(location, false, false)).
							setTransparencyState(RenderStateShard.NO_TRANSPARENCY).
							setLightmapState(RenderStateShard.LIGHTMAP).
							setOverlayState(RenderStateShard.OVERLAY).
							setCullState(RenderStateShard.NO_CULL).
							createCompositeState(true);
			return RenderType.create(
					Database.rl("triangles_solid").toString(),
					VertexFormatProvider.POSITION_TEX_NORMAL,
					VertexFormat.Mode.TRIANGLES,
					1536,
					true,
					false,
					rendertype$compositestate);
			
		}
		public static RenderType trianglesCutout(ResourceLocation location)
		{
			RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder().
					setShaderState(ShadersProvider.StateShard.TRIANGLES_CUTOUT_STATE_SHARD).
					setTextureState(new RenderStateShard.TextureStateShard(location, false, false)).
					setTransparencyState(RenderStateShard.NO_TRANSPARENCY).
					setLightmapState(RenderStateShard.LIGHTMAP).
					setOverlayState(RenderStateShard.OVERLAY).
					setCullState(RenderStateShard.NO_CULL).
					createCompositeState(true);
			return RenderType.create(
					Database.rl("triangles_cutout").toString(),
					VertexFormatProvider.POSITION_TEX_NORMAL,
					VertexFormat.Mode.TRIANGLES,
					1536,
					true,
					false,
					rendertype$compositestate);
			
		}
		public static RenderType trianglesTranslucent(ResourceLocation location)
		{
			RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder().
					setShaderState(ShadersProvider.StateShard.TRIANGLES_TRANSLUCENT_STATE_SHARD).
					setTextureState(new RenderStateShard.TextureStateShard(location, false, false)).
					setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY).
					setLightmapState(RenderStateShard.LIGHTMAP).
					setOverlayState(RenderStateShard.OVERLAY).
					setCullState(RenderStateShard.NO_CULL).
					createCompositeState(true);
			return RenderType.create(
					Database.rl("triangles_translucent").toString(),
					VertexFormatProvider.POSITION_TEX_NORMAL,
					VertexFormat.Mode.TRIANGLES,
					1536,
					true,
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
			private static final RenderStateShard.ShaderStateShard TRIANGLES_SOLID_STATE_SHARD = new RenderStateShard.ShaderStateShard(() -> ShadersProvider.TRIANGLES_SOLID_SHADER);
			private static final RenderStateShard.ShaderStateShard TRIANGLES_CUTOUT_STATE_SHARD = new RenderStateShard.ShaderStateShard(() -> ShadersProvider.TRIANGLES_SOLID_SHADER);
			private static final RenderStateShard.ShaderStateShard TRIANGLES_TRANSLUCENT_STATE_SHARD = new RenderStateShard.ShaderStateShard(() -> ShadersProvider.TRIANGLES_SOLID_SHADER);
		}
		
		@Nullable
		public static ShaderInstance TRIANGLES_SOLID_SHADER;
		@Nullable
		public static ShaderInstance TRIANGLES_CUTOUT_SHADER;
		@Nullable
		public static ShaderInstance TRIANGLES_TRANSLUCENT_SHADER;
		
		public static @Nullable ShaderInstance trianglesSolid()
		{
			return TRIANGLES_SOLID_SHADER;
		}
		public static @Nullable ShaderInstance trianglesCutout()
		{
			return TRIANGLES_CUTOUT_SHADER;
		}
		public static @Nullable ShaderInstance trianglesTranslucent()
		{
			return TRIANGLES_TRANSLUCENT_SHADER;
		}
		
		private static void registerShaders(final RegisterShadersEvent event)
		{
			try
			{
				event.registerShader(new ShaderInstance(
								event.getResourceProvider(),
								Database.rl("triangles_solid"),
								VertexFormatProvider.POSITION_TEX_NORMAL),
						shaderInstance -> TRIANGLES_SOLID_SHADER = shaderInstance);
				event.registerShader(new ShaderInstance(
								event.getResourceProvider(),
								Database.rl("triangles_cutout"),
								VertexFormatProvider.POSITION_TEX_NORMAL),
						shaderInstance -> TRIANGLES_CUTOUT_SHADER = shaderInstance);
				event.registerShader(new ShaderInstance(
								event.getResourceProvider(),
								Database.rl("triangles_translucent"),
								VertexFormatProvider.POSITION_TEX_NORMAL),
						shaderInstance -> TRIANGLES_TRANSLUCENT_SHADER = shaderInstance);
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
