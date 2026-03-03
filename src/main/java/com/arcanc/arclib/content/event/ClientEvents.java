/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arclib.content.event;


import com.arcanc.arclib.content.registration.Registration;
import com.arcanc.arclib.content.registration.block.block_entity.ber.TestBlockEntityRenderer;
import com.arcanc.arclib.content.registration.entity.renderer.TestEntityRender;
import com.arcanc.arclib.content.registration.item.renderer.TestBlockItemRenderer;
import com.arcanc.arclib.util.ArcModelCache;
import com.arcanc.arclib.util.ArcRenderTypes;
import com.arcanc.arclib.util.Database;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;

public class ClientEvents
{
	public static void registerClientEvents(final IEventBus modEventBus)
	{
		modEventBus.addListener(ClientEvents :: registerRenderers);
		modEventBus.addListener(ClientEvents :: registerReloadListeners);
		modEventBus.addListener(ClientEvents :: registerClientExtensions);
		ArcRenderTypes.register(modEventBus);
	}
	
	private static void registerClientExtensions(final RegisterSpecialModelRendererEvent event)
	{
		event.register(Database.rl("test_block"), TestBlockItemRenderer.Unbaked.MAP_CODEC);
	}
	
	private static void registerReloadListeners(final AddClientReloadListenersEvent event)
	{
		event.addListener(Database.RELOAD_LISTENER_ID, ArcModelCache :: reload);
	}
	
	private static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event)
	{
		event.registerBlockEntityRenderer(Registration.BETypeReg.TEST_BLOCK_ENTITY.get(), TestBlockEntityRenderer :: new);
		event.registerEntityRenderer(Registration.EntityTypeReg.TEST_ENTITY.get(), TestEntityRender :: new);
	}
}
