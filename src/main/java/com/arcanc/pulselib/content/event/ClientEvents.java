/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.event;


import com.arcanc.pulselib.content.animatable.PItemAnimatable;
import com.arcanc.pulselib.content.animatable.singleton.AnimationTickHandler;
import com.arcanc.pulselib.content.renderer.PRenderQueue;
import com.arcanc.pulselib.content.renderer.PRenderStagesHandler;
import com.arcanc.pulselib.util.PModelCache;
import com.arcanc.pulselib.util.PRenderTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;

public class ClientEvents
{
	public static void registerClientEvents(final IEventBus modEventBus)
	{
		//modEventBus.addListener(ClientEvents :: registerRenderers);
		
		modEventBus.addListener(ClientEvents :: registerReloadListeners);
		modEventBus.addListener(ClientEvents :: registerClientExtensions);
		NeoForge.EVENT_BUS.addListener(ClientEvents :: playerDisconnected);
		PRenderTypes.register(modEventBus);
		AnimationTickHandler.register(modEventBus);
		PRenderStagesHandler.register(modEventBus);
	}
	
	private static void registerClientExtensions(final RegisterClientExtensionsEvent event)
	{
		BuiltInRegistries.ITEM.stream().
				filter(item -> item instanceof PItemAnimatable<?>).
				forEach(item ->
						event.registerItem(((PItemAnimatable<?>) item).registerClientExtension(), item));
	}
	
	private static void registerReloadListeners(final RegisterClientReloadListenersEvent event)
	{
		event.registerReloadListener(PModelCache :: reload);
	}
	
	private static void playerDisconnected(final LevelEvent.Unload event)
	{
		if (!event.getLevel().isClientSide())
			return;
		PRenderQueue.cleanup();
	}
	
/*	private static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event)
	{
		event.registerBlockEntityRenderer(Registration.BETypeReg.TEST_BLOCK_ENTITY.get(), TestBlockEntityRenderer :: new);
		event.registerEntityRenderer(Registration.EntityTypeReg.TEST_ENTITY.get(), TestEntityRender :: new);
	}
*/}
