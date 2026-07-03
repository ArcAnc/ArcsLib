/**
 * @author ArcAnc
 * Created at: 04.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.util.armor;


import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class PulseLivingAttachments
{
	private static final Map<Item, List<PulseLivingDefinition>> ITEM_ATTACHMENTS = new Reference2ObjectLinkedOpenHashMap<>();
	private static final List<PulseLivingDefinition> GLOBAL_ATTACHMENTS = new ArrayList<>();
	
	public static void register(Item item, PulseLivingDefinition definition)
	{
		ITEM_ATTACHMENTS.computeIfAbsent(item, $ -> new ArrayList<>()).add(definition);
	}
	
	public static void register(Item item, Collection<? extends PulseLivingDefinition> definitions)
	{
		definitions.forEach(definition -> register(item, definition));
	}
	
	public static void registerGlobal(PulseLivingDefinition definition)
	{
		GLOBAL_ATTACHMENTS.add(definition);
	}
	
	public static boolean contains(Item item)
	{
		return ITEM_ATTACHMENTS.containsKey(item);
	}
	
	public static List<PulseLivingDefinition> get(ItemStack stack, EquipmentSlot slot, LivingEntity entity)
	{
		List<PulseLivingDefinition> definitions = ITEM_ATTACHMENTS.get(stack.getItem());
		if (definitions == null)
			return List.of();
		
		List<PulseLivingDefinition> result = new ArrayList<>();
		for (PulseLivingDefinition definition : definitions)
			if (definition.source().shouldRender(entity, slot, stack))
				result.add(definition);
		
		return result;
	}
	
	public static List<PulseLivingDefinition> getGlobal(LivingEntity entity)
	{
		List<PulseLivingDefinition> result = new ArrayList<>();
		for (PulseLivingDefinition definition : GLOBAL_ATTACHMENTS)
			if (definition.source().shouldRender(entity, EquipmentSlot.BODY, ItemStack.EMPTY))
				result.add(definition);
		
		return result;
	}
	
	public static boolean hidesVanillaArmor(ItemStack stack, EquipmentSlot slot, LivingEntity entity)
	{
		for (PulseLivingDefinition definition : get(stack, slot, entity))
			if (definition.hideVanilla())
				return true;
		
		return false;
	}
	
	public static Collection<Item> items()
	{
		return ITEM_ATTACHMENTS.keySet();
	}
}
