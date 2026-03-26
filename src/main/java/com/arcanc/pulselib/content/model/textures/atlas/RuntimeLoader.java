/**
 * @author ArcAnc
 * Created at: 25.03.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.model.textures.atlas;


import com.arcanc.pulselib.util.PLibDatabase;
import com.arcanc.pulselib.util.PTextureCache;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Optional;

public class RuntimeLoader implements SpriteSource
{
	public static final MapCodec<RuntimeLoader> CODEC = MapCodec.unit(RuntimeLoader ::new);
	public static final SpriteSourceType TYPE = new SpriteSourceType(CODEC);
	
	@Override
	public void run(ResourceManager resourceManager, Output output)
	{
		PTextureCache.getTextureCache().clear();
		PTextureCache.postEvent();
		PTextureCache.getTextureCache().forEach(texture ->
		{
			ResourceLocation resourcelocation = TEXTURE_ID_CONVERTER.idToFile(texture);
			Optional<Resource> optional = resourceManager.getResource(resourcelocation);
			if (optional.isPresent())
				output.add(texture, optional.get());
			else
				PLibDatabase.LOGGER.warn("Missing sprite: {}", resourcelocation);
		});
	}
	
	@Override
	public SpriteSourceType type()
	{
		return TYPE;
	}
}
