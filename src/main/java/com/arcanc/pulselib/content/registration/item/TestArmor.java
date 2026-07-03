/**
 * @author ArcAnc
 * Created at: 02.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.registration.item;


import com.arcanc.pulselib.content.renderer.modelData.PModelData;
import com.arcanc.pulselib.util.PLibDatabase;
import com.arcanc.pulselib.util.armor.PulseArmorAttachment;
import com.arcanc.pulselib.util.armor.PulseArmorDefinition;
import com.arcanc.pulselib.util.armor.PulseArmorModels;
import com.arcanc.pulselib.util.armor.PulseHumanoidAnchors;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

import java.util.List;

public class TestArmor extends ArmorItem
{
	public static final ResourceLocation TEXTURE = PLibDatabase.rl("entity/armor/test_armor/0");
	
	public TestArmor(Holder<ArmorMaterial> material, Type type, Properties properties)
	{
		super(material, type, properties);
		PulseArmorModels.register(this, createArmorDefinition(type));
	}
	
	private static PulseArmorDefinition createArmorDefinition(Type type)
	{
		return new PulseArmorDefinition(
				new PModelData.Builder(PLibDatabase.rl("armor/test_armor"), "entity").build(),
				type.getSlot(),
				attachments(type),
				true);
	}
	
	private static List<PulseArmorAttachment> attachments(Type type)
	{
		return switch (type)
		{
			case HELMET -> List.of(PulseArmorAttachment.builder(type.getSlot(), PulseHumanoidAnchors.HEAD, "head").build());
			case CHESTPLATE -> List.of(PulseArmorAttachment.builder(type.getSlot(), PulseHumanoidAnchors.RIGHT_ARM, "right_arm").build());
			case LEGGINGS -> List.of(PulseArmorAttachment.builder(type.getSlot(), PulseHumanoidAnchors.RIGHT_LEG, "right_leg").build());
			case BODY, BOOTS -> List.of();
		};
	}
}
