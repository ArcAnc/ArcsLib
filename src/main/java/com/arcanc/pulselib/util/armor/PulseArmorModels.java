/**
 * @author ArcAnc
 * Created at: 02.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.util.armor;


import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class PulseArmorModels
{
	private static final Map<Item, PulseArmorDefinition> ARMOR_MODELS = new Reference2ObjectLinkedOpenHashMap<>();
	
	public static void register(Item item, PulseArmorDefinition definition)
	{
		ARMOR_MODELS.put(item, definition);
	}
	
	public static boolean contains(Item item)
	{
		return ARMOR_MODELS.containsKey(item);
	}
	
	public static Optional<PulseArmorDefinition> get(ItemStack stack, EquipmentSlot slot)
	{
		PulseArmorDefinition definition = ARMOR_MODELS.get(stack.getItem());
		if (definition == null || !definition.slots().contains(slot))
			return Optional.empty();
		return Optional.of(definition);
	}
	
	public static Collection<Item> items()
	{
		return ARMOR_MODELS.keySet();
	}
}
