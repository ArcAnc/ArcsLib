/**
 * @author ArcAnc
 * Created at: 27.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.content.renderer.base;


import com.arcanc.arcslib.content.animatable.ArcAnimatable;
import com.arcanc.arcslib.content.model.baked.ArcBakedModel;
import com.arcanc.arcslib.content.renderer.ArcEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;

public interface ArcEntityRenderState<T extends Entity & ArcAnimatable<T>> extends ArcRenderState<T>
{
	<RS extends EntityRenderState & ArcEntityRenderState<T>> void extractEntityData(
			T entity,
			ArcEntityRenderer<T, RS> renderer);
	
	class Impl<T extends Entity & ArcAnimatable<T>> extends EntityRenderState implements ArcEntityRenderState<T>
	{
		private ArcBakedModel model;
		private T animatable;
		
		@Override
		public void extractData()
		{
		}
		
		public 	<RS extends EntityRenderState & ArcEntityRenderState<T>> void extractEntityData(
				T entity,
				ArcEntityRenderer<T, RS> renderer)
		{
			this.extractData();
			this.model = renderer.getArcModel();
			this.animatable = entity;
		}
		
		
		@Override
		public float getPartialTicks()
		{
			return this.partialTick;
		}
		
		@Override
		public ArcBakedModel getBakedModel()
		{
			return this.model;
		}
		
		@Override
		public T getAnimatable()
		{
			return this.animatable;
		}
	}
}
