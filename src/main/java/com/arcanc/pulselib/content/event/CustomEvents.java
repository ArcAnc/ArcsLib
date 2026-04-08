/**
 * @author ArcAnc
 * Created at: 01.04.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.event;


import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.Set;

public class CustomEvents
{
	public static class PLibRegisterTextureEvent extends Event implements IModBusEvent
	{
		private final Set<Identifier> registeredTextures;
		
		public PLibRegisterTextureEvent(Set<Identifier> registeredTextures)
		{
			this.registeredTextures = registeredTextures;
		}
		
		public PLibRegisterTextureEvent addTextureLocation(Identifier textureLocation)
		{
			this.registeredTextures.add(textureLocation);
			return this;
		}
	}
}
