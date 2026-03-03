/**
 * @author ArcAnc
 * Created at: 24.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arclib.content.animatable.singleton;


import com.arcanc.arclib.content.animatable.ArcAnimatable;
import com.arcanc.arclib.content.animatable.ArcAnimationManager;

public class SingletonAnimationManager<T extends ArcAnimatable<T>> extends ArcAnimationManager<T>
{
	public SingletonAnimationManager(T animatable)
	{
		super(animatable);
	}
}
