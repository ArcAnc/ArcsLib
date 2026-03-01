/**
 * @author ArcAnc
 * Created at: 28.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.content.renderer.modelData;


import net.minecraft.resources.Identifier;

public class DefaultItemModelData extends ArcModelData
{
	public DefaultItemModelData(DefaultItemModelDataBuilder builder)
	{
		super(builder);
	}
	
	public static class DefaultItemModelDataBuilder extends Builder
	{
		private final Identifier shortModelLocation;
		
		public DefaultItemModelDataBuilder(Identifier modelLocation)
		{
			super(modelLocation, "item");
			this.shortModelLocation = modelLocation;
			this.modelLocation = ArcModelData.generateDefaultModelLocation(modelLocation, this.modelType);
		}
		
		@Override
		public DefaultItemModelDataBuilder addTexture(Identifier texturePath)
		{
			super.addTexture(ArcModelData.generateDefaultTextureLocation(texturePath, this.shortModelLocation.getPath(), this.modelType));
			return this;
		}
		
		@Override
		public DefaultItemModelData build()
		{
			return new DefaultItemModelData(this);
		}
	}
}
