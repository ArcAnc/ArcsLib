/**
 * @author ArcAnc
 * Created at: 24.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.animatable;


import com.arcanc.pulselib.content.model.baked.PBakedModel;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Part of this code copied from Geckolib: <a href="https://github.com/bernie-g/geckolib/blob/1.21.1/common/src/main/java/software/bernie/geckolib/animatable/instance/AnimatableInstanceCache.java">AnimatableInstanceCache</a>
 * <p>Stop crying, Tslat!</p>
 * <p>Modified by ArcAnc</p>
 */
public class PAnimationManager<T extends PAnimatable<T>>
{
	protected static final long THRESHOLD_TIME = 5_000;
	
	protected final T animatable;
	protected PBakedModel model;
	protected final Map<String, Supplier<PAnimationController.StateHandler<T>>> factories = new Object2ObjectArrayMap<>();
	protected final Map<String, PAnimationController<T>> controllers = new Object2ObjectArrayMap<>();
	
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
		
		registrar.entries.forEach(entry -> this.factories.put(entry.name(), entry.factory()));
	}
	
	public void bindModel(PBakedModel model)
	{
		if (model != this.model)
			this.model = model;
	}
	
	public void tick()
	{
		for (PAnimationController<T> controller : this.controllers.values())
			controller.tick(this.animatable, 1, this.model);
	}
	
	public record PAnimationRegistrar<T extends PAnimatable<T>>(List<Entry<T>> entries)
	{
		public PAnimationRegistrar<T> add(Supplier<PAnimationController.StateHandler<T>> factory)
		{
			return add("default", factory);
		}
		
		public PAnimationRegistrar<T> add(String name, Supplier<PAnimationController.StateHandler<T>> factory)
		{
			this.entries.add(new Entry<>(name, factory));
			return this;
		}
		
		public record Entry<T extends PAnimatable<T>>(String name, Supplier<PAnimationController.StateHandler<T>> factory)
		{
		
		}
	}
}
