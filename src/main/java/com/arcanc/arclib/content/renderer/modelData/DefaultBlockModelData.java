/**
 * @author ArcAnc
 * Created at: 28.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arclib.content.renderer.modelData;


import net.minecraft.resources.ResourceLocation;

public class DefaultBlockModelData extends ArcModelData
{
	public DefaultBlockModelData(DefaultBlockModelDataBuilder builder)
	{
		super(builder);
	}
	
	public static class DefaultBlockModelDataBuilder extends Builder
	{
		private final ResourceLocation shortModelLocation;
		
		public DefaultBlockModelDataBuilder(ResourceLocation modelLocation)
		{
			super(modelLocation, "block");
			this.shortModelLocation = modelLocation;
			this.modelLocation = ArcModelData.generateDefaultModelLocation(modelLocation, this.modelType);
		}
		
		@Override
		public DefaultBlockModelDataBuilder addTexture(ResourceLocation texturePath)
		{
			super.addTexture(ArcModelData.generateDefaultTextureLocation(texturePath, this.shortModelLocation.getPath(), this.modelType));
			return this;
		}
		
		@Override
		public DefaultBlockModelData build()
		{
			return new DefaultBlockModelData(this);
		}
	}
}
