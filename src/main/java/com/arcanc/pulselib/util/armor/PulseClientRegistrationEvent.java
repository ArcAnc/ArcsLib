/**
 * @author ArcAnc
 * Created at: 07.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.util.armor;


import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

public class PulseClientRegistrationEvent extends Event implements IModBusEvent
{
	private final PulseClientRegistration registration = new PulseClientRegistration();
	
	public PulseClientRegistration registration()
	{
		return this.registration;
	}
}
