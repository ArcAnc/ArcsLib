/**
 * @author ArcAnc
 * Created at: 07.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.model.baked;

import com.arcanc.pulselib.content.model.deformer.PMeshDeformation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public record PMeshRenderContext(
		Function<ResourceLocation, RenderType> renderType,
		int color,
		int packedLight,
		int packedOverlay,
		@Nullable PMeshDeformation deformation,
		@Nullable ResourceLocation texture,
		@Nullable Boolean emissive)
{
	public PMeshRenderContext(Function<ResourceLocation, RenderType> renderType, int color, int packedLight, int packedOverlay)
	{
		this(renderType, color, packedLight, packedOverlay, null, null, null);
	}

	public PMeshRenderContext(Function<ResourceLocation, RenderType> renderType, int color, int packedLight, int packedOverlay,
	                          @Nullable PMeshDeformation deformation)
	{
		this(renderType, color, packedLight, packedOverlay, deformation, null, null);
	}

	public PMeshRenderContext withDeformation(@Nullable PMeshDeformation deformation)
	{
		return new PMeshRenderContext(this.renderType, this.color, this.packedLight, this.packedOverlay, deformation, this.texture, this.emissive);
	}

	public PMeshRenderContext withTexture(@Nullable ResourceLocation texture)
	{
		return new PMeshRenderContext(this.renderType, this.color, this.packedLight, this.packedOverlay, this.deformation, texture, this.emissive);
	}

	public PMeshRenderContext withEmissive(@Nullable Boolean emissive)
	{
		return new PMeshRenderContext(this.renderType, this.color, this.packedLight, this.packedOverlay, this.deformation, this.texture, emissive);
	}
}
