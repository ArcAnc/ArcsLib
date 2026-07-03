/**
 * @author ArcAnc
 * Created at: 04.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.util.armor;


import com.arcanc.pulselib.content.animatable.PAnimationController;
import com.arcanc.pulselib.content.model.baked.PBakedModel;
import com.arcanc.pulselib.content.renderer.modelData.PModelData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.List;

public class PulseLivingDefinition
{
	private final PModelData modelData;
	private final PulseLivingAttachmentSource source;
	private final List<? extends PulseLivingAttachment> attachments;
	private final boolean hideVanilla;
	private final ControllerProvider controllerProvider;
	
	public PulseLivingDefinition(PModelData modelData, List<? extends PulseLivingAttachment> attachments)
	{
		this(modelData, PulseLivingAttachmentSource.anyEquipmentSlot(), attachments, false);
	}
	
	public PulseLivingDefinition(
			PModelData modelData,
			PulseLivingAttachmentSource source,
			List<? extends PulseLivingAttachment> attachments)
	{
		this(modelData, source, attachments, false);
	}
	
	public PulseLivingDefinition(
			PModelData modelData,
			PulseLivingAttachmentSource source,
			List<? extends PulseLivingAttachment> attachments,
			boolean hideVanilla)
	{
		this(modelData, source, attachments, hideVanilla, ControllerProvider.EMPTY);
	}
	
	public PulseLivingDefinition(
			PModelData modelData,
			PulseLivingAttachmentSource source,
			List<? extends PulseLivingAttachment> attachments,
			boolean hideVanilla,
			ControllerProvider controllerProvider)
	{
		this.modelData = modelData;
		this.source = source;
		this.attachments = attachments;
		this.hideVanilla = hideVanilla;
		this.controllerProvider = controllerProvider;
	}
	
	public PModelData modelData()
	{
		return this.modelData;
	}
	
	public PulseLivingAttachmentSource source()
	{
		return this.source;
	}
	
	public List<? extends PulseLivingAttachment> attachments()
	{
		return this.attachments;
	}
	
	public boolean hideVanilla()
	{
		return this.hideVanilla;
	}
	
	public Collection<PAnimationController<?>> animationControllers(LivingEntity entity, ItemStack stack, PBakedModel model, float partialTick)
	{
		return this.controllerProvider.controllers(entity, stack, model, partialTick);
	}
	
	@FunctionalInterface
	public interface ControllerProvider
	{
		ControllerProvider EMPTY = (entity, stack, model, partialTick) -> List.of();
		
		Collection<PAnimationController<?>> controllers(LivingEntity entity, ItemStack stack, PBakedModel model, float partialTick);
	}
}
