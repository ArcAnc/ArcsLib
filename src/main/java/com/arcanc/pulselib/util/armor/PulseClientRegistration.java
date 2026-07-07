/**
 * @author ArcAnc
 * Created at: 07.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.util.armor;


import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;

public final class PulseClientRegistration
{
	private final List<Runnable> actions = new ArrayList<>();
	
	public void livingAttachment(ItemLike item, PLivingAttachmentDefinition definition)
	{
		this.actions.add(() -> PulseLivingAttachments.register(item.asItem(), definition));
	}
	
	public void globalLivingAttachment(PLivingAttachmentDefinition definition)
	{
		this.actions.add(() -> PulseLivingAttachments.registerGlobal(definition));
	}
	
	public void apply()
	{
		this.actions.forEach(Runnable :: run);
	}
}
