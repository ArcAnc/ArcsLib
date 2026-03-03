/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arclib.content.animatable;


public interface ArcAnimatable<T extends ArcAnimatable<T>>
{
	ArcAnimationManager<T> getAnimationManager();
	
	void registerAnimationControllers(ArcAnimationManager.ArcAnimationRegistrar<T> registrar);
}
