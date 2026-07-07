/**
 * @author ArcAnc
 * Created at: 04.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.util.attachments.humanoid.armor;


import com.arcanc.pulselib.util.attachments.PLivingAttachmentLayer;
import com.arcanc.pulselib.util.attachments.humanoid.PHumanoidAttachmentLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.common.NeoForge;

public class PLibArmorHandler
{
	public static void register(IEventBus modEventBus)
	{
		modEventBus.addListener(PLibArmorHandler :: addArmorLayers);
		NeoForge.EVENT_BUS.addListener(PLibArmorHandler :: renderFirstPersonArmor);
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	private static void addArmorLayers(final EntityRenderersEvent.AddLayers event)
	{
		for (PlayerSkin.Model skin : event.getSkins())
		{
			PlayerRenderer renderer = event.getSkin(skin);
			if (renderer != null)
				renderer.addLayer(new PHumanoidAttachmentLayer(renderer));
		}
		
		for (var entityType : event.getEntityTypes())
		{
			EntityRenderer<?> renderer = event.getRenderer(entityType);
			if (renderer instanceof LivingEntityRenderer livingRenderer &&
					livingRenderer.getModel() instanceof HumanoidModel)
				livingRenderer.addLayer(new PHumanoidAttachmentLayer(livingRenderer));
			else if (renderer instanceof LivingEntityRenderer livingRenderer &&
					livingRenderer.getModel() instanceof EntityModel)
				livingRenderer.addLayer(new PLivingAttachmentLayer(livingRenderer));
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
		
		PHumanoidAttachmentLayer.renderFirstPersonArm(
				event.getPoseStack(),
				event.getPackedLight(),
				event.getPlayer(),
				event.getArm(),
				event.getArm() == HumanoidArm.RIGHT ? model.rightArm : model.leftArm,
				partialTick);
	}
}
