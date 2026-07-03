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
import com.arcanc.pulselib.util.armor.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterSpriteSourceTypesEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;

public class ClientEvents
{
	private static final PulseAttachmentAnchor TEST_COW_BODY = PulseAttachmentAnchor.of(PLibDatabase.rl("cow_body"));
	
	public static void registerClientEvents(final IEventBus modEventBus)
	{
		//modEventBus.addListener(ClientEvents :: registerRenderers);
		//modEventBus.addListener(ClientEvents :: registerCustomTextures);
		PulseAttachmentAnchorResolvers.init(modEventBus);
		//registerTestCowTail();
		
		modEventBus.addListener(EventPriority.HIGHEST, ClientEvents :: registerSpriteSources);
		modEventBus.addListener(ClientEvents :: registerReloadListeners);
		modEventBus.addListener(ClientEvents :: registerClientExtensions);
		NeoForge.EVENT_BUS.addListener(ClientEvents :: playerDisconnected);
		PLibArmorHandler.register(modEventBus);
		PRenderTypes.register(modEventBus);
		PLibAnimationTicker.register(modEventBus);
		PRenderStagesHandler.register(modEventBus);
		PTextureCache.register(modEventBus);
	}
	
	/*private static void registerTestCowTail()
	{
		PulseAttachmentAnchorResolvers.register(CowModel.class, TEST_COW_BODY,
				(entity, model) -> entity.getType() == EntityType.COW ? ((CowModel<?>)model).body : null);
		PulseLivingAttachments.registerGlobal(TestTailItem.createDefinition(
				PulseLivingAttachmentSource.entityPredicate(entity -> entity.getType() == EntityType.COW),
				TEST_COW_BODY,
				new Vector3f(0, -0.4f, 0.8f),
				new Vector3f(90, 0, 0)));
	}*/
	
	private static void registerClientExtensions(final RegisterClientExtensionsEvent event)
	{
		BuiltInRegistries.ITEM.stream().
				filter(item -> item instanceof PItemAnimatable<?> || PulseLivingAttachments.contains(item)).
				forEach(item -> registerClientExtension(event, item));
	}
	
	private static void registerClientExtension(final RegisterClientExtensionsEvent event, final Item item)
	{
		if (event.isItemRegistered(item))
			return;
		
		IClientItemExtensions base = item instanceof PItemAnimatable<?> animatable ?
				animatable.registerClientExtension() :
				IClientItemExtensions.DEFAULT;
		IClientItemExtensions extension = PulseArmorClientExtensions.buildFor(item, base);
		
		if (extension != IClientItemExtensions.DEFAULT)
			event.registerItem(extension, item);
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
		InstanceAnimationManager.cleanUp();
		SingletonAnimationManager.cleanUp();
	}
	
	private static void registerSpriteSources(final RegisterSpriteSourceTypesEvent event)
	{
		event.register(PLibDatabase.rl("runtime_loader"), RuntimeLoader.TYPE);
	}
	
	/*private static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event)
	{
		//event.registerBlockEntityRenderer(Registration.BETypeReg.TEST_BLOCK_ENTITY.get(), TestBlockEntityRenderer :: new);
		event.registerEntityRenderer(Registration.EntityTypeReg.TEST_ENTITY.get(), TestEntityRender :: new);
	}*/
	
	/*private static void registerCustomTextures(final CustomEvents.PLibRegisterTextureEvent event)
	{
		event.addTextureLocation(TestEntityRender.SPHERE).
				addTextureLocation(TestEntityRender.TUBE).
				addTextureLocation(TestEntityRender.TORUS).
				addTextureLocation(TestEntityRender.ZERO).
				addTextureLocation(TestEntityRender.ARMOR);
		event.addTextureLocation(TestBlockEntityRenderer.CUBE).
				addTextureLocation(TestBlockEntityRenderer.TORUS).
				addTextureLocation(TestBlockEntityRenderer.TUBE).
				addTextureLocation(TestBlockEntityRenderer.PYRAMID);
		event.addTextureLocation(TestBlockItemRenderer.PYRAMID).
				addTextureLocation(TestBlockItemRenderer.CIRCLE);
		event.addTextureLocation(TestArmor.TEXTURE);
		event.addTextureLocation(TestTailItem.TEXTURE);
	}*/
}
