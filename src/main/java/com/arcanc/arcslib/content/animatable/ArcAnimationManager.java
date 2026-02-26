/**
 * @author ArcAnc
 * Created at: 24.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.content.animatable;


import com.arcanc.arcslib.content.animatable.instance.ArcAnimationController;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.Map;

public class ArcAnimationManager<T extends ArcAnimatable<T>>
{
	protected final T animatable;
	private final Map<String, ArcAnimationController<T>> controllers = new Object2ObjectArrayMap<>();
	
	public final Map<String, ArcAnimationController<T>> getControllers()
	{
		return this.controllers;
	}
	
	public ArcAnimationManager(final T animatable)
	{
		this.animatable = animatable;
		
		ArcAnimationRegistrar<T> registrar = new ArcAnimationRegistrar<>(new ObjectArrayList<>());
		
		this.animatable.registerAnimationControllers(registrar);
		
		registrar.controllers.forEach(controller -> this.controllers.put(controller.name(), controller));
	}
	
	public record ArcAnimationRegistrar<T extends ArcAnimatable<T>>(List<ArcAnimationController<T>> controllers)
	{
		public ArcAnimationRegistrar<T> add(ArcAnimationController<T> controllers)
		{
			this.controllers.add(controllers);
			return this;
		}
		
		public ArcAnimationRegistrar<T> remove(ArcAnimationController<T> controller)
		{
			this.controllers.remove(controller);
			return this;
		}
	}
}
