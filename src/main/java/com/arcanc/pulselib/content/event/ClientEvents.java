/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.event;


import com.arcanc.pulselib.content.animatable.PLibAnimationTicker;
import com.arcanc.pulselib.content.animatable.instance.InstanceAnimationManager;
import com.arcanc.pulselib.content.animatable.singleton.SingletonAnimationManager;
import com.arcanc.pulselib.content.model.textures.atlas.RuntimeLoader;
import com.arcanc.pulselib.content.renderer.PRenderQueue;
import com.arcanc.pulselib.content.renderer.PRenderStagesHandler;
import com.arcanc.pulselib.util.PLibDatabase;
import com.arcanc.pulselib.util.PModelCache;
import com.arcanc.pulselib.util.PRenderTypes;
import com.arcanc.pulselib.util.PTextureCache;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterSpriteSourcesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;

public class ClientEvents
{
	public static void registerClientEvents(final IEventBus modEventBus)
	{
		//modEventBus.addListener(ClientEvents :: registerRenderers);
		//modEventBus.addListener(ClientEvents :: registerCustomTextures);
		//modEventBus.addListener(ClientEvents :: registerClientExtensions);
		
		modEventBus.addListener(EventPriority.HIGHEST, ClientEvents :: registerSpriteSources);
		modEventBus.addListener(ClientEvents :: registerReloadListeners);
		NeoForge.EVENT_BUS.addListener(ClientEvents :: playerDisconnected);
		PRenderTypes.register(modEventBus);
		PLibAnimationTicker.register(modEventBus);
		PRenderStagesHandler.register(modEventBus);
		PTextureCache.register(modEventBus);
	}
	
	private static void registerReloadListeners(final AddClientReloadListenersEvent event)
	{
		event.addListener(PLibDatabase.RELOAD_LISTENER_ID, PModelCache :: reload);
	}
	
	private static void playerDisconnected(final LevelEvent.Unload event)
	{
		if (!event.getLevel().isClientSide())
			return;
		PRenderQueue.cleanUp();
		SingletonAnimationManager.cleanUp();
		InstanceAnimationManager.cleanUp();
	}
	
	private static void registerSpriteSources(final RegisterSpriteSourcesEvent event)
	{
		event.register(PLibDatabase.rl("runtime_loader"), RuntimeLoader.CODEC);
	}
	
	/*private static void registerClientExtensions(final RegisterSpecialModelRendererEvent event)
	{
		event.register(PLibDatabase.rl("test_block"), TestBlockItemRenderer.Unbaked.MAP_CODEC);
	}
	
	private static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event)
	{
		event.registerBlockEntityRenderer(Registration.BETypeReg.TEST_BLOCK_ENTITY.get(), TestBlockEntityRenderer :: new);
		event.registerEntityRenderer(Registration.EntityTypeReg.TEST_ENTITY.get(), TestEntityRender :: new);
	}
	
	private static void registerCustomTextures(final CustomEvents.PLibRegisterTextureEvent event)
	{
		event.addTextureLocation(TestBlockEntityRenderer.CUBE).
				addTextureLocation(TestBlockEntityRenderer.TORUS).
				addTextureLocation(TestBlockEntityRenderer.TUBE).
				addTextureLocation(TestBlockEntityRenderer.PYRAMID);
		event.addTextureLocation(TestEntityRender.SPHERE).
				addTextureLocation(TestEntityRender.TUBE).
				addTextureLocation(TestEntityRender.TORUS);
		event.addTextureLocation(TestBlockItemRenderer.PYRAMID).
				addTextureLocation(TestBlockItemRenderer.CIRCLE);
	}*/
}
