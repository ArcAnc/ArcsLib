package com.arcanc.arcslib;

import com.arcanc.arcslib.content.event.ClientEvents;
import com.arcanc.arcslib.content.event.CommonEvents;
import com.arcanc.arcslib.content.registration.Registration;
import com.arcanc.arcslib.util.Database;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import org.jetbrains.annotations.NotNull;

@Mod(Database.MOD_ID)
public class ArcsLib
{
    public ArcsLib(@NotNull IEventBus modEventBus, ModContainer modContainer)
    {
	    Registration.init(modEventBus);
	    
	    setupEvents(modEventBus);
    }
	
	private void setupEvents(final @NotNull IEventBus modEventBus)
	{
		CommonEvents.registerCommonEvents(modEventBus);
		if (FMLLoader.getCurrent().getDist().isClient())
			ClientEvents.registerClientEvents(modEventBus);
	}
}
