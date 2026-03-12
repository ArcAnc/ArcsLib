/**
 * @author ArcAnc
 * Created at: 24.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.animatable.instance;


import com.arcanc.pulselib.content.animatable.PAnimatable;
import com.arcanc.pulselib.content.animatable.PAnimationManager;

public class InstanceAnimationManager<T extends PAnimatable<T>> extends PAnimationManager<T>
{
	public InstanceAnimationManager(T animatable)
	{
		super(animatable);
	}
}
