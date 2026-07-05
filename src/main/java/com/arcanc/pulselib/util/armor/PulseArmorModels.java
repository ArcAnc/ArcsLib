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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.Optional;

/**
 * Armor-specific convenience facade. The actual registry is {@link PulseHumanoidAttachments}.
 */
public class PulseArmorModels
{
	public static void register(Item item, PulseArmorDefinition definition)
	{
		PulseLivingAttachments.register(item, definition);
	}

	public static boolean contains(Item item)
	{
		return PulseLivingAttachments.contains(item);
	}

	public static Optional<PulseArmorDefinition> get(ItemStack stack, EquipmentSlot slot, LivingEntity entity)
	{
		return PulseLivingAttachments.get(stack, slot, entity).stream().
				filter(PulseArmorDefinition.class :: isInstance).
				map(PulseArmorDefinition.class :: cast).
				findFirst();
	}

	public static Collection<Item> items()
	{
		return PulseLivingAttachments.items();
	}
}
