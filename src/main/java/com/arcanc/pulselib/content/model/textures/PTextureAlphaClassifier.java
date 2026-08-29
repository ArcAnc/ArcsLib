/**
 * @author ArcAnc
 * Created at: 21.08.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.model.textures;

import com.arcanc.pulselib.content.model.textures.atlas.PLibSpriteMetadata;
import com.mojang.blaze3d.platform.Transparency;
import net.minecraft.client.renderer.texture.SpriteContents;

public final class PTextureAlphaClassifier
{
	private PTextureAlphaClassifier()
	{
	}

	public static PAlphaMode resolve(SpriteContents contents)
	{
		PAlphaMode configured = contents.getAdditionalMetadata(PLibSpriteMetadata.TYPE).
				map(PLibSpriteMetadata :: alphaMode).
				orElse(PAlphaMode.AUTO);
		if (configured != PAlphaMode.AUTO)
			return configured;

		Transparency transparency = contents.transparency();
		if (transparency.hasTranslucent())
			return PAlphaMode.TRANSLUCENT;
		return transparency.hasTransparent() ? PAlphaMode.CUTOUT : PAlphaMode.OPAQUE;
	}
}
