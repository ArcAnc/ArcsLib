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
import com.arcanc.pulselib.data.PGltfModelLoader;
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
	public static final ResourceLocation DEFAULT_MODEL_FORMAT = PGltfModelLoader.INSTANCE.id();
	
	private final ResourceLocation modelLocation;
	private final String modelType;
	private final ResourceLocation modelFormat;
	private final Map<String, ResourceLocation> textures;
	
	public PModelData(Builder builder)
	{
		this.modelLocation = builder.modelLocation;
		this.modelType = builder.modelType;
		this.modelFormat = builder.modelFormat;
		this.textures = new Object2ObjectOpenHashMap<>();
		for (Pair<String, ResourceLocation> texture : builder.textures)
			this.textures.put(texture.getFirst(), texture.getSecond());
	}
	
	protected static ResourceLocation generateDefaultModelLocation(ResourceLocation modelLocation, String type)
	{
		return generateDefaultModelLocation(modelLocation, type, DEFAULT_MODEL_FORMAT);
	}
	
	protected static ResourceLocation generateDefaultModelLocation(ResourceLocation modelLocation, String type, ResourceLocation modelFormat)
	{
		return PModelCache.getModelLoader(modelFormat).
				map(loader -> loader.defaultModelLocation(modelLocation, type)).
				orElseGet(() -> PGltfModelLoader.INSTANCE.defaultModelLocation(modelLocation, type));
	}
	
	protected static ResourceLocation generateDefaultTextureLocation(ResourceLocation textureLocation, String modelLocation, String type)
	{
		return generateDefaultTextureLocation(textureLocation, ResourceLocation.withDefaultNamespace(modelLocation), type, DEFAULT_MODEL_FORMAT);
	}
	
	protected static ResourceLocation generateDefaultTextureLocation(ResourceLocation textureLocation, ResourceLocation modelLocation, String type, ResourceLocation modelFormat)
	{
		return PModelCache.getModelLoader(modelFormat).
				map(loader -> loader.defaultTextureLocation(textureLocation, modelLocation, type)).
				orElseGet(() -> PGltfModelLoader.INSTANCE.defaultTextureLocation(textureLocation, modelLocation, type));
	}
	
	public ResourceLocation getModelLocation()
	{
		return this.modelLocation;
	}
	
	public String getModelType()
	{
		return this.modelType;
	}
	
	public ResourceLocation getModelFormat()
	{
		return this.modelFormat;
	}
	
	public ResourceLocation getTextureByName(String name)
	{
		if (this.textures.get(name) == null)
		{
			ResourceLocation texturePath = PModelCache.resolveTextureLocation(this.modelLocation, name);
			this.textures.put(name, texturePath);
		}
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
		protected ResourceLocation modelFormat;
		protected List<Pair<String, ResourceLocation>> textures;
		
		public Builder(ResourceLocation modelLocation, String modelType)
		{
			this(modelLocation, modelType, DEFAULT_MODEL_FORMAT);
		}
		
		public Builder(ResourceLocation modelLocation, String modelType, ResourceLocation modelFormat)
		{
			this.modelType = modelType;
			this.modelFormat = modelFormat;
			this.modelLocation = normalizeModelLocation(modelLocation, modelType, modelFormat);
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
		
		private static ResourceLocation normalizeModelLocation(ResourceLocation modelLocation, String modelType, ResourceLocation modelFormat)
		{
			if (modelType.isEmpty())
				return modelLocation;
			
			if (PModelCache.getModelLoaders().stream().anyMatch(loader -> loader.supports(modelLocation)))
				return modelLocation;
			
			return PModelData.generateDefaultModelLocation(modelLocation, modelType, modelFormat);
		}
	}
}