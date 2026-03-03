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
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArcModelData
{
	public static final MapCodec<ArcModelData> CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(
					Identifier.CODEC.fieldOf("model_location").forGetter(ArcModelData :: getModelLocation),
					Codec.STRING.optionalFieldOf("model_type", "").forGetter(arcModelData -> ""),
					Identifier.CODEC.listOf().fieldOf("textures").forGetter(arcModelData -> new ArrayList<>(arcModelData.textures.values()))
			).apply(instance, (identifier, type, identifiers) ->
			{
				Builder builder = new Builder(identifier, type);
				for (Identifier texture : identifiers)
				{
					builder.addTexture(texture);
				}
				return builder.build();
			}));
	
	private final Identifier modelLocation;
	private final Map<String, Identifier> textures;
	
	public ArcModelData(Builder builder)
	{
		this.modelLocation = builder.modelLocation;
		this.textures = new Object2ObjectOpenHashMap<>();
		for (Identifier texture : builder.textures)
		{
			String[] textureName = texture.toString().split("/");
			String name = textureName[textureName.length - 1].substring(0, textureName[textureName.length - 1].length() - 4);
			this.textures.put(name, texture);
		}
	}
	
	protected static Identifier generateDefaultModelLocation(Identifier modelLocation, String type)
	{
		return modelLocation.withPrefix("glmodels/" + type + "/").withSuffix(".glb");
	}
	
	protected static Identifier generateDefaultTextureLocation(Identifier textureLocation, String modelLocation, String type)
	{
		return textureLocation.withPrefix("textures/" + type + "/" + modelLocation + "/").withSuffix(".png");
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
	
	public static class Builder
	{
		protected Identifier modelLocation;
		protected String modelType;
		protected List<Identifier> textures;
		
		public Builder(Identifier modelLocation, String modelType)
		{
			this.modelLocation = modelLocation;
			this.modelType = modelType;
			this.textures = new ArrayList<>();
		}
		
		public Builder addTexture(Identifier texturePath)
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
