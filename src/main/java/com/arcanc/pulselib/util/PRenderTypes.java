/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.util;


import com.arcanc.pulselib.content.mixin.RenderTypeAccessor;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

public class PRenderTypes
{
	public static class RenderTypeProvider
	{
		private static final Function<ResourceLocation, RenderType> TRIANGLES_SOLID = Util.memoize(RenderTypeProvider :: createTrianglesSolid);
		private static final Function<ResourceLocation, RenderType> TRIANGLES_CUTOUT = Util.memoize(RenderTypeProvider :: createTrianglesCutout);
		private static final Function<ResourceLocation, RenderType> TRIANGLES_TRANSLUCENT = Util.memoize(RenderTypeProvider :: createTrianglesTranslucent);
		private static final Function<ResourceLocation, RenderType> TRIANGLES_GUI = Util.memoize(RenderTypeProvider :: createTrianglesGui);
		
		private static RenderType createTrianglesSolid(ResourceLocation location)
		{
			RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder().
							setShaderState(ShadersProvider.StateShard.TRIANGLES_SOLID_STATE_SHARD).
							setTextureState(new RenderStateShard.TextureStateShard(location, false, false)).
							setTransparencyState(RenderStateShard.NO_TRANSPARENCY).
							setCullState(RenderStateShard.NO_CULL).
							setLightmapState(RenderStateShard.LIGHTMAP).
							setOverlayState(RenderStateShard.OVERLAY).
							createCompositeState(true);
			return RenderType.create(
					PLibDatabase.rl("triangles_solid").toString(),
					VertexFormatProvider.POSITION_TEX_NORMAL,
					VertexFormat.Mode.TRIANGLES,
					1536,
					true,
					false,
					rendertype$compositestate);
		}
		
		private static RenderType createTrianglesCutout(ResourceLocation location)
		{
			RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder().
					setShaderState(ShadersProvider.StateShard.TRIANGLES_CUTOUT_STATE_SHARD).
					setTextureState(new RenderStateShard.TextureStateShard(location, false, false)).
					setTransparencyState(RenderStateShard.NO_TRANSPARENCY).
					setCullState(RenderStateShard.NO_CULL).
					setLightmapState(RenderStateShard.LIGHTMAP).
					setOverlayState(RenderStateShard.OVERLAY).
					createCompositeState(true);
			return RenderType.create(
					PLibDatabase.rl("triangles_cutout").toString(),
					VertexFormatProvider.POSITION_TEX_NORMAL,
					VertexFormat.Mode.TRIANGLES,
					1536,
					true,
					false,
					rendertype$compositestate);
		}
		
		public static RenderType createTrianglesTranslucent(ResourceLocation location)
		{
			RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder().
					setShaderState(ShadersProvider.StateShard.TRIANGLES_TRANSLUCENT_STATE_SHARD).
					setTextureState(new RenderStateShard.TextureStateShard(location, false, false)).
					setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY).
					setCullState(RenderStateShard.NO_CULL).
					setLightmapState(RenderStateShard.LIGHTMAP).
					setOverlayState(RenderStateShard.OVERLAY).
					createCompositeState(true);
			return RenderType.create(
					PLibDatabase.rl("triangles_translucent").toString(),
					VertexFormatProvider.POSITION_TEX_NORMAL,
					VertexFormat.Mode.TRIANGLES,
					1536,
					true,
					false,
					rendertype$compositestate);
		}
		
		public static RenderType createTrianglesGui(ResourceLocation location)
		{
			RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder().
					setShaderState(ShadersProvider.StateShard.TRIANGLES_GUI_STATE_SHARD).
					setTextureState(new RenderStateShard.TextureStateShard(location, false, false)).
					setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY).
					setLightmapState(RenderStateShard.LIGHTMAP).
					setOverlayState(RenderStateShard.OVERLAY).
					createCompositeState(true);
			return RenderType.create(
					PLibDatabase.rl("triangles_gui").toString(),
					VertexFormatProvider.POSITION_TEX_NORMAL,
					VertexFormat.Mode.TRIANGLES,
					1536,
					true,
					false,
					rendertype$compositestate);
		}
		
		public static RenderType trianglesSolid(ResourceLocation location)
		{
			return TRIANGLES_SOLID.apply(location);
		}
		
		public static RenderType trianglesCutout(ResourceLocation location)
		{
			return TRIANGLES_CUTOUT.apply(location);
		}
		
		public static RenderType trianglesTranslucent(ResourceLocation location)
		{
			return TRIANGLES_TRANSLUCENT.apply(location);
		}
		
		public static RenderType trianglesGui(ResourceLocation location)
		{
			return TRIANGLES_GUI.apply(location);
		}
	}
	
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
			private static final RenderStateShard.ShaderStateShard TRIANGLES_CUTOUT_STATE_SHARD = new RenderStateShard.ShaderStateShard(() -> ShadersProvider.TRIANGLES_CUTOUT_SHADER);
			private static final RenderStateShard.ShaderStateShard TRIANGLES_TRANSLUCENT_STATE_SHARD = new RenderStateShard.ShaderStateShard(() -> ShadersProvider.TRIANGLES_TRANSLUCENT_SHADER);
			private static final RenderStateShard.ShaderStateShard TRIANGLES_GUI_STATE_SHARD = new RenderStateShard.ShaderStateShard(() -> ShadersProvider.TRIANGLES_GUI_SHADER);
		}
		
		@Nullable
		public static ShaderInstance TRIANGLES_SOLID_SHADER;
		@Nullable
		public static ShaderInstance TRIANGLES_CUTOUT_SHADER;
		@Nullable
		public static ShaderInstance TRIANGLES_TRANSLUCENT_SHADER;
		@Nullable
		public static ShaderInstance TRIANGLES_GUI_SHADER;
		
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
		public static @Nullable ShaderInstance trianglesGui()
		{
			return TRIANGLES_GUI_SHADER;
		}
		
		private static void registerShaders(final RegisterShadersEvent event)
		{
			try
			{
				event.registerShader(new ShaderInstance(
								event.getResourceProvider(),
								PLibDatabase.rl("triangles_solid"),
								VertexFormatProvider.POSITION_TEX_NORMAL),
						shaderInstance -> TRIANGLES_SOLID_SHADER = shaderInstance);
				event.registerShader(new ShaderInstance(
								event.getResourceProvider(),
								PLibDatabase.rl("triangles_cutout"),
								VertexFormatProvider.POSITION_TEX_NORMAL),
						shaderInstance -> TRIANGLES_CUTOUT_SHADER = shaderInstance);
				event.registerShader(new ShaderInstance(
								event.getResourceProvider(),
								PLibDatabase.rl("triangles_translucent"),
								VertexFormatProvider.POSITION_TEX_NORMAL),
						shaderInstance -> TRIANGLES_TRANSLUCENT_SHADER = shaderInstance);
				event.registerShader(new ShaderInstance(
								event.getResourceProvider(),
								PLibDatabase.rl("triangles_gui"),
								VertexFormatProvider.POSITION_TEX_NORMAL),
						shaderInstance -> TRIANGLES_GUI_SHADER = shaderInstance);
			}
			catch (IOException e)
			{
				PLibDatabase.LOGGER.warn("Failed to register shaders: {}", String.valueOf(e));
			}
		}
	}
	
	public static void register(IEventBus modEventBus)
	{
		modEventBus.addListener(ShadersProvider :: registerShaders);
	}
	
	@ApiStatus.Internal
	public static Optional<RenderStateShard.TransparencyStateShard> getTransparencyState(RenderType renderType)
	{
		if (renderType.getClass().getName().endsWith("CompositeRenderType"))
		{
			try
			{
				RenderTypeAccessor.CompositeRenderTypeAccessor composite =
						(RenderTypeAccessor.CompositeRenderTypeAccessor) renderType;
				
				RenderType.CompositeState state = composite.pulselib$getState();
				
				RenderTypeAccessor.CompositeStateAccessor stateAcc =
						(RenderTypeAccessor.CompositeStateAccessor)(Object) state;
				
				RenderStateShard.TransparencyStateShard ts = stateAcc.pulselib$getTransparencyState();
				return Optional.of(ts);
			}
			catch (ClassCastException e)
			{
				return Optional.empty();
			}
		}
		else
			return Optional.empty();
	}
}
