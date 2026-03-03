/**
 * @author ArcAnc
 * Created at: 24.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arclib.util.helpers;


import com.arcanc.arclib.content.animatable.ArcAnimatable;
import com.arcanc.arclib.content.animatable.ArcAnimationManager;
import com.arcanc.arclib.content.animatable.instance.InstanceAnimationManager;
import com.arcanc.arclib.content.animatable.singleton.SingletonAnimationManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.NonNull;

public class ArcLibHelper
{
	public static <T extends ArcAnimatable<T>> @NonNull ArcAnimationManager<T> createManager(@NonNull T animatable)
	{
		if (animatable instanceof BlockEntity || animatable instanceof Entity)
			return createManager(animatable, false);
		return createManager(animatable, true);
	}
	
	public static <T extends ArcAnimatable<T>> @NonNull ArcAnimationManager<T> createManager(@NonNull T animatable, boolean singleton)
	{
		ArcAnimationManager<T> manager = animatable.getAnimationManager();
		
		if (manager != null)
			return manager;
		
		return singleton ? new SingletonAnimationManager<>(animatable) : new InstanceAnimationManager<>(animatable);
	}
}
