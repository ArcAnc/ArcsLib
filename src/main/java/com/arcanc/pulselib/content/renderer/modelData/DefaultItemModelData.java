/**
 * @author ArcAnc
 * Created at: 28.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.renderer.modelData;


import net.minecraft.resources.ResourceLocation;

public class DefaultItemModelData extends PModelData
{
	public DefaultItemModelData(DefaultItemModelDataBuilder builder)
	{
		super(builder);
	}
	
	public static class DefaultItemModelDataBuilder extends Builder
	{
		private final ResourceLocation shortModelLocation;
		
		public DefaultItemModelDataBuilder(ResourceLocation modelLocation)
		{
			super(modelLocation, "item");
			this.shortModelLocation = modelLocation;
			this.modelLocation = PModelData.generateDefaultModelLocation(modelLocation, this.modelType);
		}
		
		@Override
		public DefaultItemModelDataBuilder addTexture(ResourceLocation texturePath)
		{
			super.addTexture(PModelData.generateDefaultTextureLocation(texturePath, this.shortModelLocation.getPath(), this.modelType));
			return this;
		}
		
		@Override
		public DefaultItemModelData build()
		{
			return new DefaultItemModelData(this);
		}
	}
}
