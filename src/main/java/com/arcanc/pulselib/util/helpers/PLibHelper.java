/**
 * @author ArcAnc
 * Created at: 24.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.util.helpers;


import com.arcanc.pulselib.content.animatable.AnimManagerKey;
import com.arcanc.pulselib.content.animatable.PAnimatable;
import com.arcanc.pulselib.content.animatable.PAnimationManager;
import com.arcanc.pulselib.content.animatable.instance.InstanceAnimationManager;
import com.arcanc.pulselib.content.animatable.singleton.SingletonAnimationManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PLibHelper
{
	public static <T extends PAnimatable<T>> PAnimationManager<T> createManager(T animatable)
	{
		if (animatable instanceof BlockEntity || animatable instanceof Entity)
			return createManager(animatable, false);
		return createManager(animatable, true);
	}
	
	public static <T extends PAnimatable<T>> PAnimationManager<T> createManager(T animatable, boolean singleton)
	{
		AnimManagerKey key = AnimManagerKey.ofObject(animatable);
		PAnimationManager<T> manager = animatable.getAnimationManager(key);
		
		if (manager != null)
			return manager;
		
		return singleton ? SingletonAnimationManager.getManager(key, animatable) : new InstanceAnimationManager<>(animatable);
	}
}
