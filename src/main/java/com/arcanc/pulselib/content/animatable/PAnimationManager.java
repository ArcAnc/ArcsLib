/**
 * @author ArcAnc
 * Created at: 24.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.animatable;


import com.arcanc.pulselib.content.animatable.instance.PAnimationController;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.Map;

/**
 * Part of this code copied from Geckolib: <a href="https://github.com/bernie-g/geckolib/blob/1.21.1/common/src/main/java/software/bernie/geckolib/animatable/instance/AnimatableInstanceCache.java">AnimatableInstanceCache</a>
 * <p>Stop crying, Tslat!</p>
 * <p>Modified by ArcAnc</p>
 */
public class PAnimationManager<T extends PAnimatable<T>>
{
	protected final T animatable;
	private final Map<String, PAnimationController<T>> controllers = new Object2ObjectArrayMap<>();
	
	public Map<String, PAnimationController<T>> getControllers()
	{
		return this.controllers;
	}
	
	public T getAnimatable()
	{
		return this.animatable;
	}
	
	public PAnimationManager(final T animatable)
	{
		this.animatable = animatable;
		
		PAnimationRegistrar<T> registrar = new PAnimationRegistrar<>(new ObjectArrayList<>());
		
		this.animatable.registerAnimationControllers(registrar);
		
		registrar.controllers.forEach(controller -> this.controllers.put(controller.name(), controller));
	}
	
	public record PAnimationRegistrar<T extends PAnimatable<T>>(List<PAnimationController<T>> controllers)
	{
		public PAnimationRegistrar<T> add(PAnimationController<T> controllers)
		{
			this.controllers.add(controllers);
			return this;
		}
		
		public PAnimationRegistrar<T> remove(PAnimationController<T> controller)
		{
			this.controllers.remove(controller);
			return this;
		}
	}
}
