/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.content.renderer;


import com.arcanc.arcslib.content.model.baked.ArcBakedModel;
import com.arcanc.arcslib.util.ArcModelCache;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;

import java.util.Map;

public class ArcModelData
{
	private final Identifier modelLocation;
	private final Map<String, Identifier> textures;
	
	public ArcModelData(Identifier modelLocation, String modelType, Identifier... textures)
	{
		this.modelLocation = generateDefaultModelLocation(modelLocation, modelType);
		this.textures = new Object2ObjectOpenHashMap<>();
		for (Identifier texture : textures)
		{
			String[] textureName = texture.toString().split("/");
			String name = textureName[textureName.length - 1].substring(0, textureName[textureName.length - 1].length() - 4);
			this.textures.put(name, texture);
		}
	}
	
	private Identifier generateDefaultModelLocation(Identifier modelLocation, String type)
	{
		return modelLocation.withPrefix("glmodels/" + type + "/").withSuffix(".glb");
	}
	
	public Identifier getModelLocation()
	{
		return this.modelLocation;
	}
	
	public Identifier getTextureByName(String name)
	{
		return this.textures.getOrDefault(name, TextureManager.INTENTIONAL_MISSING_TEXTURE);
	}
	
	public ArcBakedModel getModel()
	{
		return ArcModelCache.getModels().get(this.modelLocation);
	}
}
