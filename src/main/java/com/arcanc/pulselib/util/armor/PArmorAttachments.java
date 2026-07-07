/**
 * @author ArcAnc
 * Created at: 07.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.util.armor;


import com.arcanc.pulselib.content.renderer.modelData.PModelData;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.List;

public final class PArmorAttachments
{
	private PArmorAttachments()
	{
	}
	
	public static PLivingAttachmentDefinition humanoidArmor(
			PModelData model,
			EquipmentSlot slot,
			List<PAttachmentBinding> bindings,
			boolean hideVanilla)
	{
		return new PLivingAttachmentDefinition(
				model,
				PLivingAttachmentSources.equipmentSlot(slot),
				bindings,
				PMeshRenderResolvers.defaultLit(),
				hideVanilla);
	}
}
