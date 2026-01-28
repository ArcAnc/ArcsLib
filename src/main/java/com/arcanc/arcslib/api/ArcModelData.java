/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.api;


import com.arcanc.arcslib.content.model.ArcModel;
import com.arcanc.arcslib.content.model.baked.ArcBakedModel;
import com.arcanc.arcslib.util.ArcModelCache;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class ArcModelData
{
	private final Identifier modelLocation;
	private final Identifier[] textures;
	
	public ArcModelData(Identifier modelLocation, String modelType, Identifier... textures)
	{
		this.modelLocation = generateDefaultModelLocation(modelLocation, modelType);
		this.textures = textures;
	}
	
	private @NotNull Identifier generateDefaultModelLocation(@NotNull Identifier modelLocation, String type)
	{
		return modelLocation.withPrefix("bbmodels/" + type + "/").withSuffix(".bbmodel");
	}
	
	public Identifier getModelLocation()
	{
		return this.modelLocation;
	}
	
	public Identifier[] getTextures()
	{
		return this.textures;
	}
	
	public Identifier getTextureById(int id)
	{
		int checkId = Mth.clamp(id, 0, this.textures.length - 1);
		return this.textures[checkId];
	}
	
	public ArcBakedModel getModel()
	{
		return ArcModelCache.getModels().get(this.modelLocation);
	}
}
