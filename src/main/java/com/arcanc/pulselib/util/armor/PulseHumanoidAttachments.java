/**
 * @author ArcAnc
 * Created at: 03.07.2026
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
import java.util.List;

public class PulseHumanoidAttachments
{
	public static void register(Item item, PulseHumanoidDefinition definition)
	{
		PulseLivingAttachments.register(item, definition);
	}
	
	public static void register(Item item, Collection<? extends PulseHumanoidDefinition> definitions)
	{
		PulseLivingAttachments.register(item, definitions);
	}
	
	public static void registerGlobal(PulseHumanoidDefinition definition)
	{
		PulseLivingAttachments.registerGlobal(definition);
	}
	
	public static boolean contains(Item item)
	{
		return PulseLivingAttachments.contains(item);
	}
	
	public static List<PulseLivingDefinition> get(ItemStack stack, EquipmentSlot slot, LivingEntity entity)
	{
		return PulseLivingAttachments.get(stack, slot, entity);
	}
	
	public static List<PulseLivingDefinition> getGlobal(LivingEntity entity)
	{
		return PulseLivingAttachments.getGlobal(entity);
	}
	
	public static boolean hidesVanillaArmor(ItemStack stack, EquipmentSlot slot, LivingEntity entity)
	{
		return PulseLivingAttachments.hidesVanillaArmor(stack, slot, entity);
	}
	
	public static Collection<Item> items()
	{
		return PulseLivingAttachments.items();
	}
}
