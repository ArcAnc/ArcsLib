/**
 * @author ArcAnc
 * Created at: 25.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.util.helpers;


import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

public class MathHelper
{
	public static Quaternionf eulerToQuaternion(@NonNull Vector3f euler)
	{
		return new Quaternionf().
				rotateXYZ(Mth.DEG_TO_RAD * euler.x(),
				          Mth.DEG_TO_RAD * euler.y(),
						         Mth.DEG_TO_RAD * euler.z());
	}
}
