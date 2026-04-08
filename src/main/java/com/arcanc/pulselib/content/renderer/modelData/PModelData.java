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
import com.arcanc.pulselib.util.PLibDatabase;
import com.arcanc.pulselib.util.PModelCache;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PModelData
{
	public static final MapCodec<PModelData> CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(
					Identifier.CODEC.fieldOf("model_location").forGetter(PModelData :: getModelLocation),
					Codec.STRING.optionalFieldOf("model_type", "").forGetter(pModelData -> ""),
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
	
	public PModelData(Builder builder)
	{
		this.modelLocation = builder.modelLocation;
		this.textures = new Object2ObjectOpenHashMap<>();
		for (Pair<String, Identifier> texture : builder.textures)
			this.textures.put(texture.getFirst(), texture.getSecond());
	}
	
	protected static Identifier generateDefaultModelLocation(Identifier modelLocation, String type)
	{
		return modelLocation.withPrefix("glmodels/" + type + "/").withSuffix(".glb");
	}
	
	protected static Identifier generateDefaultTextureLocation(Identifier textureLocation, String modelLocation, String type)
	{
		return textureLocation.withPrefix(type + "/" + modelLocation + "/");
	}
	
	public Identifier getModelLocation()
	{
		return this.modelLocation;
	}
	
	public Identifier getTextureByName(String name)
	{
		if (this.textures.get(name) == null)
		{
			String modelPath = this.modelLocation.getPath().substring(0, this.modelLocation.getPath().length() - 4);
			String[] divided = modelPath.split("/");
			Identifier texturePath = this.modelLocation.withPath(divided[1] + "/" + divided[2] + "/" + name);
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
		protected Identifier modelLocation;
		protected String modelType;
		protected List<Pair<String, Identifier>> textures;
		
		public Builder(Identifier modelLocation, String modelType)
		{
			this.modelLocation = modelLocation;
			this.modelType = modelType;
			this.textures = new ArrayList<>();
		}
		
		public Builder addTexture(Identifier texturePath)
		{
			String[] parsedName = texturePath.getPath().split("/");
			String textureName = parsedName[parsedName.length - 1];
			return this.addTexture(textureName.contains(".png") ? textureName.substring(0, textureName.length() - 4): textureName, texturePath);
		}
		
		public Builder addTexture(String textureName, Identifier textureLocation)
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
