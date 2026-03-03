/**
 * @author ArcAnc
 * Created at: 25.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arclib.util.helpers;


import net.minecraft.client.Minecraft;
import org.jspecify.annotations.NonNull;

public class RenderHelper
{
	public static @NonNull Minecraft mc()
	{
		return Minecraft.getInstance();
	}
}
