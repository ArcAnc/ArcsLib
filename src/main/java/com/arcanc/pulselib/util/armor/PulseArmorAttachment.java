/**
 * @author ArcAnc
 * Created at: 02.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.util.armor;


import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import org.joml.Vector3f;

import java.util.function.Function;

public record PulseArmorAttachment(
		EquipmentSlot slot,
		VanillaHumanoidPart vanillaPart,
		String pulseBone,
		Function<ResourceLocation, RenderType> renderType,
		Vector3f offset,
		int color)
{
	public PulseArmorAttachment(
			EquipmentSlot slot,
			VanillaHumanoidPart vanillaPart,
			String pulseBone,
			Function<ResourceLocation, RenderType> renderType,
			int color)
	{
		this(slot, vanillaPart, pulseBone, renderType, new Vector3f(), color);
	}
	
	public PulseArmorAttachment(
			EquipmentSlot slot,
			VanillaHumanoidPart vanillaPart,
			String pulseBone,
			Function<ResourceLocation, RenderType> renderType)
	{
		this(slot, vanillaPart, pulseBone, renderType, new Vector3f(), -1);
	}
}
