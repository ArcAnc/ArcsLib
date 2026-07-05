/**
 * @author ArcAnc
 * Created at: 05.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.util.armor;

import net.minecraft.resources.Identifier;

public record PulseAttachmentAnchor(Identifier id)
{
	public static PulseAttachmentAnchor of(Identifier id)
	{
		return new PulseAttachmentAnchor(id);
	}

	public static PulseAttachmentAnchor minecraft(String path)
	{
		return of(Identifier.withDefaultNamespace(path));
	}
}
