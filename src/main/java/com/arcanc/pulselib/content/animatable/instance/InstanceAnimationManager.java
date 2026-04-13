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
import com.arcanc.pulselib.content.animatable.PAnimationController;
import com.arcanc.pulselib.content.animatable.PAnimationManager;
import com.arcanc.pulselib.util.PLibDatabase;
import com.arcanc.pulselib.util.helpers.PLibRenderHelper;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Util;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.Objects;
import java.util.Set;

/**
 * Part of this code copied from Geckolib: <a href="https://github.com/bernie-g/geckolib/blob/1.21.1/common/src/main/java/software/bernie/geckolib/animatable/instance/InstancedAnimatableInstanceCache.java">InstancedAnimatableInstanceCache</a>
 * <p>Stop crying, Tslat!</p>
 * <p>Modified by ArcAnc</p>
 */
@EventBusSubscriber (modid = PLibDatabase.MOD_ID)
public class InstanceAnimationManager<T extends PAnimatable<T>> extends PAnimationManager<T>
{
	private static final Set<AnimationManagerContainer<?>> MANAGERS = new ObjectOpenHashSet<>();
	
	public InstanceAnimationManager(T animatable)
	{
		super(animatable);
		this.factories.forEach((name, supplier) -> this.controllers.put(name, new PAnimationController<>(name, supplier.get())));
	}
	
	public static void tickAll()
	{
		MANAGERS.forEach(container ->
				container.manager().tick());
	}
	
	public static void addManager(PAnimationManager<?> manager)
	{
		if (manager instanceof InstanceAnimationManager<?> instance)
			MANAGERS.add(new AnimationManagerContainer<>(Util.getEpochMillis(), instance));
	}
	
	@SubscribeEvent
	public static void removeUnused(final ClientTickEvent.Post event)
	{
		ClientLevel level = PLibRenderHelper.mc().level;
		if (level == null)
			return;
		if (level.getGameTime() % 200 != 0)
			return;
		if (MANAGERS.isEmpty())
			return;
		
		long now = Util.getEpochMillis();
		
		MANAGERS.removeIf(container -> container.lastUsedTick() + THRESHOLD_TIME < now);
	}
	
	public static void cleanUp()
	{
		MANAGERS.clear();
	}
	
	static class AnimationManagerContainer<T extends PAnimatable<T>>
	{
		long lastUsedTick;
		InstanceAnimationManager<T> manager;
		
		AnimationManagerContainer(long lastUsedTick, InstanceAnimationManager<T> manager)
		{
			this.lastUsedTick = lastUsedTick;
			this.manager = manager;
		}
		
		InstanceAnimationManager<T> manager()
		{
			return this.manager;
		}
		
		long lastUsedTick()
		{
			return this.lastUsedTick;
		}
		
		@Override
		public boolean equals(Object o)
		{
			if (! (o instanceof AnimationManagerContainer<?> that))
				return false;
			return Objects.equals(this.manager, that.manager);
		}
		
		@Override
		public int hashCode()
		{
			return Objects.hashCode(this.manager);
		}
	}
}
