/**
 * @author ArcAnc
 * Created at: 23.05.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.renderer.modelData;


import net.minecraft.resources.ResourceLocation;

public class DefaultEntityLayerModelData extends PModelData
{
	public DefaultEntityLayerModelData(DefaultEntityLayerModelDataBuilder builder)
	{
		super(builder);
	}
	
	public static class DefaultEntityLayerModelDataBuilder extends Builder
	{
		private final ResourceLocation shortModelLocation;
		
		public DefaultEntityLayerModelDataBuilder(ResourceLocation entityType, ResourceLocation shortModelLocation)
		{
			this(entityType, shortModelLocation, PModelData.DEFAULT_MODEL_FORMAT);
		}
		
		public DefaultEntityLayerModelDataBuilder(ResourceLocation entityType, ResourceLocation shortModelLocation, ResourceLocation modelFormat)
		{
			super(shortModelLocation, "entity");
			this.shortModelLocation = shortModelLocation.withPrefix(entityType.getPath() + "/");
			this.modelFormat = modelFormat;
			this.modelLocation = PModelData.generateDefaultModelLocation(this.shortModelLocation, this.modelType, this.modelFormat);
		}
		
		@Override
		public DefaultEntityLayerModelDataBuilder addTexture(ResourceLocation texturePath)
		{
			super.addTexture(PModelData.generateDefaultTextureLocation(texturePath, this.shortModelLocation, this.modelType, this.modelFormat));
			return this;
		}
		
		@Override
		public DefaultEntityLayerModelData build()
		{
			return new DefaultEntityLayerModelData(this);
		}
	}
}
