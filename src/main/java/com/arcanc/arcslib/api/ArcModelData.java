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
import com.arcanc.arcslib.util.ArcModelCache;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class ArcModelData
{
	private final Identifier modelLocation;
	
	public ArcModelData(Identifier modelLocation, String modelType)
	{
		this.modelLocation = generateDefaultModelLocation(modelLocation, modelType);
	}
	
	private @NotNull Identifier generateDefaultModelLocation(@NotNull Identifier modelLocation, String type)
	{
		return modelLocation.withPrefix("bbmodels/" + type + "/").withSuffix(".bbmodel");
	}
	
	public Identifier getModelLocation()
	{
		return this.modelLocation;
	}
	
	public ArcModel getModel()
	{
		return ArcModelCache.getModels().get(this.modelLocation);
	}
}
