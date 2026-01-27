/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.content.event;


import com.arcanc.arcslib.content.block.block_entity.ber.TestBlockEntityRenderer;
import com.arcanc.arcslib.content.registration.Registration;
import com.arcanc.arcslib.util.ArcModelCache;
import com.arcanc.arcslib.util.ArcRenderTypes;
import com.arcanc.arcslib.util.Database;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.jetbrains.annotations.NotNull;

public class ClientEvents
{
	public static void registerClientEvents(final @NotNull IEventBus modEventBus)
	{
		modEventBus.addListener(ClientEvents :: registerRenderers);
		modEventBus.addListener(ClientEvents :: registerReloadListeners);
		ArcRenderTypes.register(modEventBus);
	}
	
	private static void registerReloadListeners(final @NotNull AddClientReloadListenersEvent event)
	{
		event.addListener(Database.RELOAD_LISTENER_ID, ArcModelCache :: reload);
	}
	
	
	private static void registerRenderers(final EntityRenderersEvent.@NotNull RegisterRenderers event)
	{
		event.registerBlockEntityRenderer(Registration.BETypeReg.TEST_BLOCK_ENTITY.get(), TestBlockEntityRenderer :: new);
	}
}
