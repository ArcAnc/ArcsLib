/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arclib.content.renderer.modelData;


import com.arcanc.arclib.content.model.baked.ArcBakedModel;
import com.arcanc.arclib.util.ArcModelCache;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArcModelData
{
	public static final MapCodec<ArcModelData> CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(
					ResourceLocation.CODEC.fieldOf("model_location").forGetter(ArcModelData :: getModelLocation),
					Codec.STRING.optionalFieldOf("model_type", "").forGetter(arcModelData -> ""),
					ResourceLocation.CODEC.listOf().fieldOf("textures").forGetter(arcModelData -> new ArrayList<>(arcModelData.textures.values()))
			).apply(instance, (ResourceLocation, type, ResourceLocations) ->
			{
				Builder builder = new Builder(ResourceLocation, type);
				for (ResourceLocation texture : ResourceLocations)
				{
					builder.addTexture(texture);
				}
				return builder.build();
			}));
	
	private final ResourceLocation modelLocation;
	private final Map<String, ResourceLocation> textures;
	
	public ArcModelData(Builder builder)
	{
		this.modelLocation = builder.modelLocation;
		this.textures = new Object2ObjectOpenHashMap<>();
		for (ResourceLocation texture : builder.textures)
		{
			String[] textureName = texture.toString().split("/");
			String name = textureName[textureName.length - 1].substring(0, textureName[textureName.length - 1].length() - 4);
			this.textures.put(name, texture);
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
	
	public ArcBakedModel getModel()
	{
		return ArcModelCache.getModels().get(this.modelLocation);
	}
	
	public static class Builder
	{
		protected ResourceLocation modelLocation;
		protected String modelType;
		protected List<ResourceLocation> textures;
		
		public Builder(ResourceLocation modelLocation, String modelType)
		{
			this.modelLocation = modelLocation;
			this.modelType = modelType;
			this.textures = new ArrayList<>();
		}
		
		public Builder addTexture(ResourceLocation texturePath)
		{
			this.textures.add(texturePath);
			return this;
		}
		
		public ArcModelData build()
		{
			return new ArcModelData(this);
		}
	}
}
