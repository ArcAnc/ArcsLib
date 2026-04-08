/**
 * @author ArcAnc
 * Created at: 05.04.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.animatable;


import com.arcanc.pulselib.content.animatable.instance.InstanceAnimationManager;
import com.arcanc.pulselib.content.animatable.singleton.SingletonAnimationManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public class PLibAnimationTicker
{
	public static void register(final IEventBus modEvenBus)
	{
		NeoForge.EVENT_BUS.addListener(PLibAnimationTicker :: clientTick);
		NeoForge.EVENT_BUS.register(SingletonAnimationManager.class);
		NeoForge.EVENT_BUS.register(InstanceAnimationManager.class);
	}
	
	private static void clientTick(final LevelTickEvent.Pre event)
	{
		if (!event.getLevel().isClientSide())
			return;
		SingletonAnimationManager.tickAll();
		InstanceAnimationManager.tickAll();
	}
}
