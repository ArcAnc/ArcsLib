/**
 * @author ArcAnc
 * Created at: 26.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.util;


import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Database
{
	public static final String MOD_ID = "arcslib";
	public static final String MOD_NAME = "Arc's Lib";
	private static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
	
	public static final Identifier RELOAD_LISTENER_ID = rl("models_reload_listener");
	
	
	public static @NotNull Identifier rl(String name)
	{
		return Identifier.fromNamespaceAndPath(MOD_ID, name);
	}
	
	public static Logger getLogger()
	{
		return LOGGER;
	}
}
