/**
 * @author ArcAnc
 * Created at: 08.03.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.animatable.singleton;


import com.arcanc.pulselib.content.animatable.PAnimatable;
import com.arcanc.pulselib.content.animatable.PAnimationManager;
import com.arcanc.pulselib.content.animatable.instance.PAnimationController;
import com.arcanc.pulselib.content.renderer.PRenderer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import java.util.Map;

public class AnimationTickHandler
{
	private static final Map<ResourceLocation, AnimationDataStorage<?>> SINGLETON_MANAGERS = new Object2ObjectOpenHashMap<>();
	
	public static void register(final IEventBus modEventBus)
	{
		NeoForge.EVENT_BUS.addListener(AnimationTickHandler :: animationTicker);
		NeoForge.EVENT_BUS.addListener(AnimationTickHandler :: tagsUpdatedEvent);
	}
	
	private static void animationTicker(final RenderFrameEvent.Pre event)
	{
		float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
		for (AnimationDataStorage<?> dataStorage : SINGLETON_MANAGERS.values())
			dataStorage.tick(partialTick);
	}
	
	private static void tagsUpdatedEvent(final TagsUpdatedEvent event)
	{
		if (event.shouldUpdateStaticData())
			return;
		SINGLETON_MANAGERS.clear();
		event.getRegistryAccess().lookupOrThrow(Registries.ITEM).
				filterElements(item -> item instanceof PAnimatable<?>).
				listElements().
				forEach(itemRef ->
				{
					Item item = itemRef.value();
					SINGLETON_MANAGERS.put(itemRef.key().location(), createStorage(item));
				});
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends PAnimatable<T>> AnimationDataStorage<T> createStorage(Item item)
	{
		T animatable = (T) item;
		IClientItemExtensions ext = IClientItemExtensions.of(item);
		PRenderer<T> renderer = (PRenderer<T>) ext.getCustomRenderer();
		return new AnimationDataStorage<>(animatable, renderer, animatable.getAnimationManager());
	}
	
	private record AnimationDataStorage<T extends PAnimatable<T>>(T animatable, PRenderer<T> renderer, PAnimationManager<T> animationManager)
	{
		void tick(float partialTick)
		{
			for (PAnimationController<T> controller : this.animationManager.getControllers().values())
					controller.tick(this.animatable, this.renderer.getModel(animatable), partialTick);
		}
	}
}
