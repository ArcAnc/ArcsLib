/**
 * @author ArcAnc
 * Created at: 25.03.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.event;


import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.Set;

public class CustomEvents
{
	public static class PLibRegisterTextureEvent extends Event implements IModBusEvent
	{
		private final Set<ResourceLocation> registeredTextures;
		
		public PLibRegisterTextureEvent(Set<ResourceLocation> registeredTextures)
		{
			this.registeredTextures = registeredTextures;
		}
		
		public PLibRegisterTextureEvent addTextureLocation(ResourceLocation textureLocation)
		{
			this.registeredTextures.add(textureLocation);
			return this;
		}
	}
}
