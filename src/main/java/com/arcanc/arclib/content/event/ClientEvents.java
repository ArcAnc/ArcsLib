/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arclib.content.event;


import com.arcanc.arclib.content.animatable.singleton.AnimationTickHandler;
import com.arcanc.arclib.content.registration.Registration;
import com.arcanc.arclib.content.registration.block.block_entity.ber.TestBlockEntityRenderer;
import com.arcanc.arclib.content.registration.entity.renderer.TestEntityRender;
import com.arcanc.arclib.util.ArcModelCache;
import com.arcanc.arclib.util.ArcRenderTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

public class ClientEvents
{
	public static void registerClientEvents(final IEventBus modEventBus)
	{
		modEventBus.addListener(ClientEvents :: registerRenderers);
		modEventBus.addListener(ClientEvents :: registerReloadListeners);
		modEventBus.addListener(ClientEvents :: registerClientExtensions);
		ArcRenderTypes.register(modEventBus);
		AnimationTickHandler.register(modEventBus);
	}
	
	private static void registerClientExtensions(final RegisterClientExtensionsEvent event)
	{
		event.registerItem(Registration.ItemReg.TEST_ITEM.get().registerExtension(), Registration.ItemReg.TEST_ITEM);
	}
	
	private static void registerReloadListeners(final RegisterClientReloadListenersEvent event)
	{
		event.registerReloadListener(ArcModelCache :: reload);
	}
	
	private static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event)
	{
		event.registerBlockEntityRenderer(Registration.BETypeReg.TEST_BLOCK_ENTITY.get(), TestBlockEntityRenderer :: new);
		event.registerEntityRenderer(Registration.EntityTypeReg.TEST_ENTITY.get(), TestEntityRender :: new);
	}
}
