/**
 * @author ArcAnc
 * Created at: 25.03.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.util;


import com.arcanc.pulselib.content.event.CustomEvents;
import com.arcanc.pulselib.util.helpers.PLibRenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.RegisterMaterialAtlasesEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class PTextureCache
{
	public static final ResourceLocation ATLAS_LOCATION = PLibDatabase.rl("textures/atlas.png");
	@ApiStatus.Internal
	public static final ResourceLocation ATLAS_FILE_LOCATION = PLibDatabase.rl("atlas");
	private static @Nullable TextureAtlas TEXTURES;
	
	private static final Set<ResourceLocation> TEXTURE_CACHE = new HashSet<>();
	
	public static TextureAtlas getTextureAtlas()
	{
		if (TEXTURES == null)
			TEXTURES = PLibRenderHelper.mc().getModelManager().getAtlas(ATLAS_LOCATION);
		
		return TEXTURES;
	}
	
	public static void register(IEventBus modEventBus)
	{
		modEventBus.addListener(PTextureCache :: registerAtlas);
	}
	
	private static void registerAtlas(final RegisterMaterialAtlasesEvent event)
	{
		event.register(ATLAS_LOCATION, ATLAS_FILE_LOCATION);
	}
	
	@ApiStatus.Internal
	public static Set<ResourceLocation> getTextureCache()
	{
		return TEXTURE_CACHE;
	}
	
	@ApiStatus.Internal
	public static void postEvent()
	{
		ModLoader.postEvent(new CustomEvents.PLibRegisterTextureEvent(TEXTURE_CACHE));
	}
}
