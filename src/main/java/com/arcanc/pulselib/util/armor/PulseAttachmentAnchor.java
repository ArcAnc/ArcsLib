/**
 * @author ArcAnc
 * Created at: 04.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.util.armor;


import net.minecraft.resources.ResourceLocation;

public record PulseAttachmentAnchor(ResourceLocation id)
{
	public static PulseAttachmentAnchor of(ResourceLocation id)
	{
		return new PulseAttachmentAnchor(id);
	}
	
	public static PulseAttachmentAnchor minecraft(String path)
	{
		return of(ResourceLocation.withDefaultNamespace(path));
	}
}
