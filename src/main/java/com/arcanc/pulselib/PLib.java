package com.arcanc.pulselib;

import com.arcanc.pulselib.content.event.ClientEvents;
import com.arcanc.pulselib.content.event.CommonEvents;
import com.arcanc.pulselib.content.registration.Registration;
import com.arcanc.pulselib.util.Database;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;

@Mod(Database.MOD_ID)
public class PLib
{
    public PLib(IEventBus modEventBus, ModContainer modContainer)
    {
	    Registration.init(modEventBus);
	    
	    setupEvents(modEventBus);
    }
	
	private void setupEvents(final IEventBus modEventBus)
	{
		CommonEvents.registerCommonEvents(modEventBus);
		if (FMLLoader.getDist().isClient())
			ClientEvents.registerClientEvents(modEventBus);
	}
}
