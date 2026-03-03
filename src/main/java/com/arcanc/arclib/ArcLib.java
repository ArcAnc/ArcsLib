package com.arcanc.arclib;

import com.arcanc.arclib.content.event.ClientEvents;
import com.arcanc.arclib.content.event.CommonEvents;
import com.arcanc.arclib.content.registration.Registration;
import com.arcanc.arclib.util.Database;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;

@Mod(Database.MOD_ID)
public class ArcLib
{
    public ArcLib(IEventBus modEventBus, ModContainer modContainer)
    {
	    Registration.init(modEventBus);
	    
	    setupEvents(modEventBus);
    }
	
	private void setupEvents(final IEventBus modEventBus)
	{
		CommonEvents.registerCommonEvents(modEventBus);
		if (FMLLoader.getCurrent().getDist().isClient())
			ClientEvents.registerClientEvents(modEventBus);
	}
}
