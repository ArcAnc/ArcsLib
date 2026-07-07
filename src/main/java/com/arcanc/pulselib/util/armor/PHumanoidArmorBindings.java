/**
 * @author ArcAnc
 * Created at: 07.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.util.armor;


import java.util.List;

public final class PHumanoidArmorBindings
{
	private PHumanoidArmorBindings()
	{
	}
	
	public static List<PAttachmentBinding> chest(String body, String rightArm, String leftArm)
	{
		return List.of(
				PHumanoidBindings.body(body),
				PHumanoidBindings.rightArm(rightArm),
				PHumanoidBindings.leftArm(leftArm));
	}
}
