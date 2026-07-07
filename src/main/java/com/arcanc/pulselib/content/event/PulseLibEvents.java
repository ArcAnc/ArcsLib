/**
 * @author ArcAnc
 * Created at: 25.03.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.event;


import com.arcanc.pulselib.util.attachments.PLivingAttachmentDefinition;
import com.arcanc.pulselib.util.attachments.PLivingAttachments;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PulseLibEvents
{
	public static class RegisterTextureEvent extends Event implements IModBusEvent
	{
		private final Set<Identifier> registeredTextures;
		
		public RegisterTextureEvent(Set<Identifier> registeredTextures)
		{
			this.registeredTextures = registeredTextures;
		}
		
		public RegisterTextureEvent addTextureLocation(Identifier textureLocation)
		{
			this.registeredTextures.add(textureLocation);
			return this;
		}
	}
	
	public static class AttachmentRegistrationEvent extends Event implements IModBusEvent
	{
		private final AttachmentRegistration registration = new AttachmentRegistration();
		
		public AttachmentRegistration registration()
		{
			return this.registration;
		}
		
		public static final class AttachmentRegistration
		{
			private final List<Runnable> actions = new ArrayList<>();
			
			public void registerLiving(ItemLike item, PLivingAttachmentDefinition definition)
			{
				this.actions.add(() -> PLivingAttachments.register(item.asItem(), definition));
			}
			
			public void registerGlobalLiving(PLivingAttachmentDefinition definition)
			{
				this.actions.add(() -> PLivingAttachments.registerGlobal(definition));
			}
			
			public void apply()
			{
				this.actions.forEach(Runnable :: run);
			}
		}
	}
}
