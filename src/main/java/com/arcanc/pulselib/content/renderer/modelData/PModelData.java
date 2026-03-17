/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.renderer.modelData;


import com.arcanc.pulselib.content.model.baked.PBakedModel;
import com.arcanc.pulselib.util.PModelCache;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PModelData
{
	private final ResourceLocation modelLocation;
	private final Map<String, ResourceLocation> textures;
	
	public PModelData(Builder builder)
	{
		this.modelLocation = builder.modelLocation;
		this.textures = new Object2ObjectOpenHashMap<>();
		for (Pair<String, ResourceLocation> texture : builder.textures)
		{
			this.textures.put(texture.getFirst(), texture.getSecond());
		}
	}
	
	protected static ResourceLocation generateDefaultModelLocation(ResourceLocation modelLocation, String type)
	{
		return modelLocation.withPrefix("glmodels/" + type + "/").withSuffix(".glb");
	}
	
	protected static ResourceLocation generateDefaultTextureLocation(ResourceLocation textureLocation, String modelLocation, String type)
	{
		return textureLocation.withPrefix("textures/" + type + "/" + modelLocation + "/").withSuffix(".png");
	}
	
	public ResourceLocation getModelLocation()
	{
		return this.modelLocation;
	}
	
	public ResourceLocation getTextureByName(String name)
	{
		return this.textures.getOrDefault(name, TextureManager.INTENTIONAL_MISSING_TEXTURE);
	}
	
	public @Nullable PBakedModel getModel()
	{
		if (PModelCache.getModels() == null)
			return null;
		return PModelCache.getModels().get(this.modelLocation);
	}
	
	public static class Builder
	{
		protected ResourceLocation modelLocation;
		protected String modelType;
		protected List<Pair<String, ResourceLocation>> textures;
		
		public Builder(ResourceLocation modelLocation, String modelType)
		{
			this.modelLocation = modelLocation;
			this.modelType = modelType;
			this.textures = new ArrayList<>();
		}
		
		public Builder addTexture(ResourceLocation texturePath)
		{
			String[] parsedName = texturePath.getPath().split("/");
			String textureName = parsedName[parsedName.length - 1];
			return this.addTexture(textureName.contains(".png") ? textureName.substring(0, textureName.length() - 4): textureName, texturePath);
		}
		
		public Builder addTexture(String textureName, ResourceLocation textureLocation)
		{
			this.textures.add(new Pair<>(textureName, textureLocation));
			return this;
		}
		
		public PModelData build()
		{
			return new PModelData(this);
		}
	}
}
