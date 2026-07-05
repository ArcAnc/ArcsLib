/**
 * @author ArcAnc
 * Created at: 05.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.util.armor;


import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

@FunctionalInterface
public interface PulseLivingAttachmentSource
{
	boolean shouldRender(LivingEntity entity, EquipmentSlot slot, ItemStack stack);

	static PulseLivingAttachmentSource anyEquipmentSlot()
	{
		return (entity, slot, stack) -> true;
	}

	static PulseLivingAttachmentSource equipmentSlot(EquipmentSlot slot)
	{
		return (entity, currentSlot, stack) -> currentSlot == slot;
	}

	static PulseLivingAttachmentSource hand()
	{
		return (entity, slot, stack) -> slot.getType() == EquipmentSlot.Type.HAND;
	}

	static PulseLivingAttachmentSource entityPredicate(Predicate<LivingEntity> predicate)
	{
		return (entity, slot, stack) -> predicate.test(entity);
	}

	static PulseLivingAttachmentSource equipmentSlotPredicate(EquipmentSlot slot, Predicate<LivingEntity> predicate)
	{
		return (entity, currentSlot, stack) -> currentSlot == slot && predicate.test(entity);
	}
}
