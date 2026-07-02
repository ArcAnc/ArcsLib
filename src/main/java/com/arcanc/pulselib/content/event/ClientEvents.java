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
import com.arcanc.pulselib.util.armor.PArmorLayer;
import com.arcanc.pulselib.util.armor.PulseArmorClientExtensions;
import com.arcanc.pulselib.util.armor.PulseArmorModels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterSpriteSourceTypesEvent;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;

public class ClientEvents
{
	public static void registerClientEvents(final IEventBus modEventBus)
	{
		//modEventBus.addListener(ClientEvents :: registerRenderers);
		//modEventBus.addListener(ClientEvents :: registerCustomTextures);
		
		modEventBus.addListener(EventPriority.HIGHEST, ClientEvents :: registerSpriteSources);
		modEventBus.addListener(ClientEvents :: registerReloadListeners);
		modEventBus.addListener(ClientEvents :: registerClientExtensions);
		modEventBus.addListener(ClientEvents :: addArmorLayers);
		NeoForge.EVENT_BUS.addListener(ClientEvents :: playerDisconnected);
		NeoForge.EVENT_BUS.addListener(ClientEvents :: renderFirstPersonArmor);
		PRenderTypes.register(modEventBus);
		PLibAnimationTicker.register(modEventBus);
		PRenderStagesHandler.register(modEventBus);
		PTextureCache.register(modEventBus);
	}
	
	private static void registerClientExtensions(final RegisterClientExtensionsEvent event)
	{
		BuiltInRegistries.ITEM.stream().
				filter(item -> item instanceof PItemAnimatable<?> || PulseArmorModels.contains(item)).
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
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	private static void addArmorLayers(final EntityRenderersEvent.AddLayers event)
	{
		for (PlayerSkin.Model skin : event.getSkins())
		{
			PlayerRenderer renderer = event.getSkin(skin);
			if (renderer != null)
				renderer.addLayer(new PArmorLayer(renderer));
		}
		
		for (var entityType : event.getEntityTypes())
		{
			EntityRenderer<?> renderer = event.getRenderer(entityType);
			if (renderer instanceof LivingEntityRenderer livingRenderer &&
					livingRenderer.getModel() instanceof HumanoidModel)
				livingRenderer.addLayer(new PArmorLayer(livingRenderer));
		}
	}
	
	private static void renderFirstPersonArmor(final RenderArmEvent event)
	{
		Minecraft mc = Minecraft.getInstance();
		EntityRenderer<?> renderer = mc.getEntityRenderDispatcher().getRenderer(event.getPlayer());
		if (!(renderer instanceof PlayerRenderer playerRenderer))
			return;
		
		HumanoidModel<?> model = playerRenderer.getModel();
		float partialTick = mc.isPaused() ? 0 : mc.getTimer().getGameTimeDeltaPartialTick(false);
		
		PArmorLayer.renderFirstPersonArm(
				event.getPoseStack(),
				event.getPackedLight(),
				event.getPlayer(),
				event.getArm(),
				event.getArm() == HumanoidArm.RIGHT ? model.rightArm : model.leftArm,
				partialTick);
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
	}*/
}
