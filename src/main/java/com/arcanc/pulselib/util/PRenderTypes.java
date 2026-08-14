/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.util;


import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class PRenderTypes
{
	public static class RenderPipelinesProvider
	{
		private static final Set<RenderPipeline> PIPELINES = new HashSet<>();
		private static final DepthStencilState DEPTH_TEST_NO_WRITE =
				new DepthStencilState(DepthStencilState.DEFAULT.depthTest(), false);
		private static final BindGroupLayout TRIANGLES_LAYOUT = BindGroupLayout.builder().
				withSampler("Sampler0").
				withSampler("Sampler2").
				withUniform("Lighting", UniformType.UNIFORM_BUFFER).
				withUniform("InstanceData", UniformType.UNIFORM_BUFFER).
				withUniform("DeformerOperations", UniformType.TEXEL_BUFFER, GpuFormat.RGBA32_FLOAT).
				withUniform("DeformerValues", UniformType.TEXEL_BUFFER, GpuFormat.RGBA32_FLOAT).
				build();
		private static final BindGroupLayout TRIANGLES_INSTANT_LAYOUT = BindGroupLayout.builder().
				withSampler("Sampler0").
				withSampler("Sampler1").
				withSampler("Sampler2").
				withUniform("Lighting", UniformType.UNIFORM_BUFFER).
				withUniform("ColorOverlay", UniformType.UNIFORM_BUFFER).
				withUniform("DeformerOperations", UniformType.TEXEL_BUFFER, GpuFormat.RGBA32_FLOAT).
				withUniform("DeformerValues", UniformType.TEXEL_BUFFER, GpuFormat.RGBA32_FLOAT).
				build();

		private static final RenderPipeline.Snippet TRIANGLES_SNIPPET = RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET).
				withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION).
				withVertexShader(PLibDatabase.rl("core/triangles")).
				withFragmentShader(PLibDatabase.rl("core/triangles")).
				withBindGroupLayout(TRIANGLES_LAYOUT).
				withVertexBinding(0, VertexFormatProvider.POSITION_TEX_NORMAL).
				withPrimitiveTopology(PrimitiveTopology.TRIANGLES).
				withDepthStencilState(DepthStencilState.DEFAULT).
				buildSnippet();
		
		public static final RenderPipeline TRIANGLES_SOLID = registerPipeline(RenderPipeline.builder(TRIANGLES_SNIPPET).
				withLocation(PLibDatabase.rl("pipeline/triangles_solid_no_cull")).
				withBindGroupLayout(BindGroupLayouts.SAMPLER1).
				withShaderDefine("OPAQUE").
				withCull(false).
				build());
		
		public static final RenderPipeline TRIANGLES_CUTOUT = registerPipeline(RenderPipeline.builder(TRIANGLES_SNIPPET).
				withLocation(PLibDatabase.rl("pipeline/triangles_cutout_no_cull")).
				withBindGroupLayout(BindGroupLayouts.SAMPLER1).
				withShaderDefine("ALPHA_CUTOUT", 0.1F).
				withCull(false).
				build());
		
		public static final RenderPipeline TRIANGLES_TRANSLUCENT = registerPipeline(RenderPipeline.builder(TRIANGLES_SNIPPET).
				withLocation(PLibDatabase.rl("pipeline/triangles_translucent_no_cull")).
				withBindGroupLayout(BindGroupLayouts.SAMPLER1).
				withShaderDefine("ALPHA_CUTOUT", 0.1F).
				withCull(false).
				withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT)).
				withDepthStencilState(DEPTH_TEST_NO_WRITE).
				build());

		public static final RenderPipeline TRIANGLES_EMISSIVE_CUTOUT = registerPipeline(RenderPipeline.builder(TRIANGLES_SNIPPET).
				withLocation(PLibDatabase.rl("pipeline/triangles_emissive_cutout_no_cull")).
				withBindGroupLayout(BindGroupLayouts.SAMPLER1).
				withShaderDefine("EMISSIVE").
				withShaderDefine("ALPHA_CUTOUT", 0.1F).
				withCull(false).
				build());

		public static final RenderPipeline TRIANGLES_EMISSIVE_TRANSLUCENT = registerPipeline(RenderPipeline.builder(TRIANGLES_SNIPPET).
				withLocation(PLibDatabase.rl("pipeline/triangles_emissive_translucent_no_cull")).
				withBindGroupLayout(BindGroupLayouts.SAMPLER1).
				withShaderDefine("EMISSIVE").
				withShaderDefine("ALPHA_CUTOUT", 0.1F).
				withCull(false).
				withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT)).
				withDepthStencilState(DEPTH_TEST_NO_WRITE).
				build());
		
		private static final RenderPipeline.Snippet TRIANGLES_INSTANT_SNIPPET = RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET).
				withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION).
				withVertexShader(PLibDatabase.rl("core/triangles_instant")).
				withFragmentShader(PLibDatabase.rl("core/triangles_instant")).
				withBindGroupLayout(TRIANGLES_INSTANT_LAYOUT).
				withCull(false).
				withDepthStencilState(DepthStencilState.DEFAULT).
				withVertexBinding(0, VertexFormatProvider.POSITION_TEX_NORMAL).
				withPrimitiveTopology(PrimitiveTopology.TRIANGLES).
				buildSnippet();

		public static final RenderPipeline TRIANGLES_INSTANT_CUTOUT = registerPipeline(RenderPipeline.builder(TRIANGLES_INSTANT_SNIPPET).
				withLocation(PLibDatabase.rl("pipeline/triangles_instant_cutout")).
				withShaderDefine("ALPHA_CUTOUT", 0.1F).
				build());

		public static final RenderPipeline TRIANGLES_INSTANT_TRANSLUCENT = registerPipeline(RenderPipeline.builder(TRIANGLES_INSTANT_SNIPPET).
				withLocation(PLibDatabase.rl("pipeline/triangles_instant_translucent")).
				withShaderDefine("ALPHA_CUTOUT", 0.1F).
				withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT)).
				withDepthStencilState(DEPTH_TEST_NO_WRITE).
				build());

		public static final RenderPipeline TRIANGLES_INSTANT_EMISSIVE_CUTOUT = registerPipeline(RenderPipeline.builder(TRIANGLES_INSTANT_SNIPPET).
				withLocation(PLibDatabase.rl("pipeline/triangles_instant_emissive_cutout")).
				withShaderDefine("EMISSIVE").
				withShaderDefine("ALPHA_CUTOUT", 0.1F).
				build());

		public static final RenderPipeline TRIANGLES_INSTANT_EMISSIVE_TRANSLUCENT = registerPipeline(RenderPipeline.builder(TRIANGLES_INSTANT_SNIPPET).
				withLocation(PLibDatabase.rl("pipeline/triangles_instant_emissive_translucent")).
				withShaderDefine("EMISSIVE").
				withShaderDefine("ALPHA_CUTOUT", 0.1F).
				withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT)).
				withDepthStencilState(DEPTH_TEST_NO_WRITE).
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
	}
	
	public static class VertexFormatProvider
	{
		public static final VertexFormat POSITION_TEX_NORMAL = VertexFormat.builder(0).
				addAttribute("Position", GpuFormat.RGB32_FLOAT).
				addAttribute("UV0", GpuFormat.RG32_FLOAT).
				addAttribute("Normal", GpuFormat.RGBA8_SNORM).
				build();
	}
	
	public static class RenderTypeProvider
	{
		private static final Function<Identifier, RenderType> TRIANGLES_SOLID = Util.memoize(RenderTypeProvider :: createTrianglesSolid);
		private static final Function<Identifier, RenderType> TRIANGLES_CUTOUT = Util.memoize(RenderTypeProvider :: createTrianglesCutout);
		private static final Function<Identifier, RenderType> TRIANGLES_TRANSLUCENT = Util.memoize(RenderTypeProvider :: createTrianglesTranslucent);
		private static final Function<Identifier, RenderType> TRIANGLES_EMISSIVE_CUTOUT = Util.memoize(RenderTypeProvider :: createTrianglesEmissiveCutout);
		private static final Function<Identifier, RenderType> TRIANGLES_EMISSIVE_TRANSLUCENT = Util.memoize(RenderTypeProvider :: createTrianglesEmissiveTranslucent);
		private static final Function<Identifier, RenderType> TRIANGLES_INSTANT_CUTOUT = Util.memoize(RenderTypeProvider :: createTrianglesInstantCutout);
		private static final Function<Identifier, RenderType> TRIANGLES_INSTANT_TRANSLUCENT = Util.memoize(RenderTypeProvider :: createTrianglesInstantTranslucent);
		private static final Function<Identifier, RenderType> TRIANGLES_INSTANT_EMISSIVE_CUTOUT = Util.memoize(RenderTypeProvider :: createTrianglesInstantEmissiveCutout);
		private static final Function<Identifier, RenderType> TRIANGLES_INSTANT_EMISSIVE_TRANSLUCENT = Util.memoize(RenderTypeProvider :: createTrianglesInstantEmissiveTranslucent);
		
		private static RenderType createTrianglesSolid(Identifier texture)
		{
			RenderSetup setup = RenderSetup.builder(RenderPipelinesProvider.TRIANGLES_SOLID).
					withTexture("Sampler0", texture).
					useLightmap().
					useOverlay().
					affectsCrumbling().
					setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE).
					createRenderSetup();
			
			return RenderType.create(PLibDatabase.rl("triangles_solid").toString(), setup);
		}
		
		private static RenderType createTrianglesCutout(Identifier texture)
		{
			RenderSetup setup = RenderSetup.builder(RenderPipelinesProvider.TRIANGLES_CUTOUT).
					withTexture("Sampler0", texture).
					useLightmap().
					useOverlay().
					affectsCrumbling().
					setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE).
					createRenderSetup();
			
			return RenderType.create(PLibDatabase.rl("triangles_cutout").toString(), setup);
		}
		
		private static RenderType createTrianglesTranslucent(Identifier texture)
		{
			RenderSetup setup = RenderSetup.builder(RenderPipelinesProvider.TRIANGLES_TRANSLUCENT).
					withTexture("Sampler0", texture).
					useLightmap().
					useOverlay().
					affectsCrumbling().
					setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE).
					createRenderSetup();
			
			return RenderType.create(PLibDatabase.rl("triangles_translucent").toString(), setup);
		}

		private static RenderType createTrianglesEmissiveCutout(Identifier texture)
		{
			RenderSetup setup = RenderSetup.builder(RenderPipelinesProvider.TRIANGLES_EMISSIVE_CUTOUT).
					withTexture("Sampler0", texture).
					useOverlay().
					affectsCrumbling().
					setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE).
					createRenderSetup();

			return RenderType.create(PLibDatabase.rl("triangles_emissive_cutout").toString(), setup);
		}

		private static RenderType createTrianglesEmissiveTranslucent(Identifier texture)
		{
			RenderSetup setup = RenderSetup.builder(RenderPipelinesProvider.TRIANGLES_EMISSIVE_TRANSLUCENT).
					withTexture("Sampler0", texture).
					useOverlay().
					affectsCrumbling().
					setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE).
					createRenderSetup();

			return RenderType.create(PLibDatabase.rl("triangles_emissive_translucent").toString(), setup);
		}
		
		private static RenderType createTrianglesInstantCutout(Identifier texture)
		{
			RenderSetup setup = RenderSetup.builder(RenderPipelinesProvider.TRIANGLES_INSTANT_CUTOUT).
					withTexture("Sampler0", texture).
					useLightmap().
					useOverlay().
					setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE).
					createRenderSetup();

			return RenderType.create(PLibDatabase.rl("triangles_instant_cutout").toString(), setup);
		}

		private static RenderType createTrianglesInstantTranslucent(Identifier texture)
		{
			RenderSetup setup = RenderSetup.builder(RenderPipelinesProvider.TRIANGLES_INSTANT_TRANSLUCENT).
					withTexture("Sampler0", texture).
					useLightmap().
					useOverlay().
					setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE).
					createRenderSetup();

			return RenderType.create(PLibDatabase.rl("triangles_instant_translucent").toString(), setup);
		}

		private static RenderType createTrianglesInstantEmissiveCutout(Identifier texture)
		{
			RenderSetup setup = RenderSetup.builder(RenderPipelinesProvider.TRIANGLES_INSTANT_EMISSIVE_CUTOUT).
					withTexture("Sampler0", texture).
					useOverlay().
					setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE).
					createRenderSetup();

			return RenderType.create(PLibDatabase.rl("triangles_instant_emissive_cutout").toString(), setup);
		}

		private static RenderType createTrianglesInstantEmissiveTranslucent(Identifier texture)
		{
			RenderSetup setup = RenderSetup.builder(RenderPipelinesProvider.TRIANGLES_INSTANT_EMISSIVE_TRANSLUCENT).
					withTexture("Sampler0", texture).
					useOverlay().
					setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE).
					createRenderSetup();

			return RenderType.create(PLibDatabase.rl("triangles_instant_emissive_translucent").toString(), setup);
		}
		
		public static RenderType trianglesSolid(Identifier texture)
		{
			return TRIANGLES_SOLID.apply(texture);
		}
		
		public static RenderType trianglesCutout(Identifier texture)
		{
			return TRIANGLES_CUTOUT.apply(texture);
		}
		
		public static RenderType trianglesTranslucent(Identifier texture)
		{
			return TRIANGLES_TRANSLUCENT.apply(texture);
		}

		public static RenderType trianglesEmissiveCutout(Identifier texture)
		{
			return TRIANGLES_EMISSIVE_CUTOUT.apply(texture);
		}

		public static RenderType trianglesEmissiveTranslucent(Identifier texture)
		{
			return TRIANGLES_EMISSIVE_TRANSLUCENT.apply(texture);
		}
		
		public static RenderType trianglesGui(Identifier texture)
		{
			return trianglesInstantTranslucent(texture);
		}

		public static RenderType trianglesInstantCutout(Identifier texture)
		{
			return TRIANGLES_INSTANT_CUTOUT.apply(texture);
		}

		public static RenderType trianglesInstantTranslucent(Identifier texture)
		{
			return TRIANGLES_INSTANT_TRANSLUCENT.apply(texture);
		}

		public static RenderType trianglesInstantEmissiveCutout(Identifier texture)
		{
			return TRIANGLES_INSTANT_EMISSIVE_CUTOUT.apply(texture);
		}

		public static RenderType trianglesInstantEmissiveTranslucent(Identifier texture)
		{
			return TRIANGLES_INSTANT_EMISSIVE_TRANSLUCENT.apply(texture);
		}

		public static RenderType emissiveVariant(RenderType baseType, Identifier texture)
		{
			return isTransparent(baseType) ? trianglesEmissiveTranslucent(texture) : trianglesEmissiveCutout(texture);
		}

		public static RenderType instantVariant(RenderType baseType, Identifier texture)
		{
			return isTransparent(baseType) ? trianglesInstantTranslucent(texture) : trianglesInstantCutout(texture);
		}

		public static RenderType instantEmissiveVariant(RenderType baseType, Identifier texture)
		{
			return isTransparent(baseType) ? trianglesInstantEmissiveTranslucent(texture) : trianglesInstantEmissiveCutout(texture);
		}
	}
	
	public static void register(IEventBus modEventBus)
	{
		modEventBus.addListener(RenderPipelinesProvider :: registerCustomPipelines);
	}
	
	@ApiStatus.Internal
	public static boolean isTransparent(RenderType renderType)
	{
		return renderType.pipeline().getColorTargetState().blendFunction().isPresent();
	}
	
}
