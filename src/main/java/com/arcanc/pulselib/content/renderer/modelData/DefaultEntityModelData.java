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

public class DefaultEntityModelData extends PModelData
{
	public DefaultEntityModelData(DefaultEntityModelDataBuilder builder)
	{
		super(builder);
	}
	
	public static class DefaultEntityModelDataBuilder extends Builder
	{
		private final ResourceLocation shortModelLocation;
		
		public DefaultEntityModelDataBuilder(ResourceLocation modelLocation)
		{
			super(modelLocation, "entity");
			this.shortModelLocation = modelLocation;
			this.modelLocation = PModelData.generateDefaultModelLocation(modelLocation, this.modelType);
		}
		
		@Override
		public DefaultEntityModelDataBuilder addTexture(ResourceLocation texturePath)
		{
			super.addTexture(PModelData.generateDefaultTextureLocation(texturePath, this.shortModelLocation.getPath(), this.modelType));
			return this;
		}
		
		@Override
		public DefaultEntityModelData build()
		{
			return new DefaultEntityModelData(this);
		}
	}
}
