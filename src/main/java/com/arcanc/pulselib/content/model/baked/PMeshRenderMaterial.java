/**
 * @author ArcAnc
 * Created at: 14.08.2026
 * Copyright (c) 2026
 */

package com.arcanc.pulselib.content.model.baked;

import net.minecraft.util.LightCoordsUtil;

public record PMeshRenderMaterial(PBakedMesh mesh, boolean emissive, int packedLight)
{
	public static PMeshRenderMaterial resolve(PBakedMesh mesh, PMeshRenderContext context)
	{
		PBakedMesh texturedMesh = PMeshTextureVariants.resolve(mesh, context.texture());
		boolean emissive = context.emissive() == null ? texturedMesh.isEmissive() : context.emissive();
		return new PMeshRenderMaterial(texturedMesh, emissive,
				emissive ? LightCoordsUtil.FULL_BRIGHT : context.packedLight());
	}
}
