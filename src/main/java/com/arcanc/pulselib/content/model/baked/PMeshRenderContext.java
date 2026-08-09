/**
 * @author ArcAnc
 * Created at: 07.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.model.baked;

<<<<<<< HEAD

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
=======
import com.arcanc.pulselib.content.model.deformer.PMeshDeformation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
>>>>>>> a625c91 (Added deformers for player and custom models)

import java.util.function.Function;

public record PMeshRenderContext(
		Function<Identifier, RenderType> renderType,
		int color,
		int packedLight,
		int packedOverlay,
		PMeshDeformation deformation)
{
	public PMeshRenderContext(Function<ResourceLocation, RenderType> renderType, int color, int packedLight, int packedOverlay)
	{
		this(renderType, color, packedLight, packedOverlay, null);
	}

	public PMeshRenderContext withDeformation(PMeshDeformation deformation)
	{
		return new PMeshRenderContext(this.renderType, this.color, this.packedLight, this.packedOverlay, deformation);
	}
}
