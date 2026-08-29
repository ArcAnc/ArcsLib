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
import com.arcanc.pulselib.content.model.textures.PAlphaMode;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

public record PMeshRenderContext(
		Function<Identifier, RenderType> renderType,
		int color,
		int packedLight,
		int packedOverlay,
		@Nullable PMeshDeformation deformation,
		@Nullable Identifier texture,
		@Nullable Boolean emissive,
		@Nullable PAlphaMode alphaModeOverride)
{
	public PMeshRenderContext(Function<Identifier, RenderType> renderType, int color, int packedLight, int packedOverlay)
	{
		this(renderType, color, packedLight, packedOverlay, null, null, null, null);
	}

	public PMeshRenderContext(Function<Identifier, RenderType> renderType, int color, int packedLight, int packedOverlay,
	                          @Nullable PMeshDeformation deformation, @Nullable Identifier texture, @Nullable Boolean emissive)
	{
		this(renderType, color, packedLight, packedOverlay, deformation, texture, emissive, null);
	}

	public PMeshRenderContext withDeformation(@Nullable PMeshDeformation deformation)
	{
		return new PMeshRenderContext(renderType, color, packedLight, packedOverlay, deformation, texture, emissive, alphaModeOverride);
	}

	public PMeshRenderContext withTexture(@Nullable Identifier texture)
	{
		return new PMeshRenderContext(renderType, color, packedLight, packedOverlay, deformation, texture, emissive, alphaModeOverride);
	}

	public PMeshRenderContext withEmissive(@Nullable Boolean emissive)
	{
		return new PMeshRenderContext(renderType, color, packedLight, packedOverlay, deformation, texture, emissive, alphaModeOverride);
	}

	public PMeshRenderContext withColor(int color)
	{
		return new PMeshRenderContext(renderType, color, packedLight, packedOverlay, deformation, texture, emissive, alphaModeOverride);
	}

	public PMeshRenderContext withPackedLight(int packedLight)
	{
		return new PMeshRenderContext(renderType, color, packedLight, packedOverlay, deformation, texture, emissive, alphaModeOverride);
	}

	public PMeshRenderContext withPackedOverlay(int packedOverlay)
	{
		return new PMeshRenderContext(renderType, color, packedLight, packedOverlay, deformation, texture, emissive, alphaModeOverride);
	}

	public PMeshRenderContext withAlphaMode(@Nullable PAlphaMode alphaModeOverride)
	{
		return new PMeshRenderContext(renderType, color, packedLight, packedOverlay, deformation, texture, emissive, alphaModeOverride);
	}
}
