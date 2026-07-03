/**
 * @author ArcAnc
 * Created at: 02.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.util.armor;


import com.arcanc.pulselib.content.renderer.modelData.PModelData;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.List;

public class PulseArmorDefinition extends PulseHumanoidDefinition
{
	private final EquipmentSlot slot;
	private final List<PulseArmorAttachment> armorAttachments;
	
	public PulseArmorDefinition(
			PModelData modelData,
			EquipmentSlot slot,
			List<PulseArmorAttachment> attachments,
			boolean hideVanilla)
	{
		super(modelData, PulseLivingAttachmentSource.equipmentSlot(slot), attachments, hideVanilla);
		this.slot = slot;
		this.armorAttachments = attachments;
	}
	
	public EquipmentSlot slot()
	{
		return this.slot;
	}
	
	public List<PulseArmorAttachment> armorAttachments()
	{
		return this.armorAttachments;
	}
}
