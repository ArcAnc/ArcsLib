/**
 * @author ArcAnc
 * Created at: 27.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.renderer.base;


import com.arcanc.pulselib.content.animatable.AnimManagerKey;
import com.arcanc.pulselib.content.animatable.PAnimatable;
import com.arcanc.pulselib.content.model.baked.PBakedModel;
import com.arcanc.pulselib.content.renderer.PEntityRenderer;
import com.arcanc.pulselib.util.helpers.PLibRenderHelper;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.jspecify.annotations.Nullable;

public interface PEntityRenderState<T extends Entity & PAnimatable<T>> extends PRenderState<T>
{
	<RS extends EntityRenderState & PEntityRenderState<T>> void extractEntityData(
			T entity,
			PEntityRenderer<T, RS> renderer);
	
	class Impl<T extends Entity & PAnimatable<T>> extends EntityRenderState implements PEntityRenderState<T>
	{
		private @Nullable PBakedModel model;
		private T animatable;
		private AnimManagerKey key;
		
		@Override
		public void extractData()
		{
			this.partialTick = PLibRenderHelper.mc().isPaused() ? 0 : this.partialTick;
		}
		
		@Override
		public <RS extends EntityRenderState & PEntityRenderState<T>> void extractEntityData(
				T entity,
				PEntityRenderer<T, RS> renderer)
		{
			this.extractData();
			this.model = renderer.getModel((RS) this);
			this.animatable = entity;
			this.key = AnimManagerKey.of(entity);
		}
		
		@Override
		public float partialTick()
		{
			return this.partialTick;
		}
		
		@Override
		public @Nullable PBakedModel getBakedModel()
		{
			return this.model;
		}
		
		@Override
		public T getAnimatable()
		{
			return this.animatable;
		}
		
		@Override
		public AnimManagerKey getAnimKey()
		{
			return this.key;
		}
	}
	
	class LivingImpl<T extends LivingEntity & PAnimatable<T>> extends LivingEntityRenderState implements PEntityRenderState<T>
	{
		private @Nullable PBakedModel model;
		private T animatable;
		private AnimManagerKey key;
		private float headYRot;
		private float headXRot;
		
		@Override
		public void extractData()
		{
			this.partialTick = PLibRenderHelper.mc().isPaused() ? 0 : this.partialTick;
		}
		
		@Override
		public <RS extends EntityRenderState & PEntityRenderState<T>> void extractEntityData(
				T entity,
				PEntityRenderer<T, RS> renderer)
		{
			this.extractData();
			this.model = renderer.getModel((RS) this);
			this.animatable = entity;
			this.key = AnimManagerKey.of(entity);
		}
		
		public void extractHeadData(float headYRot, float headXRot)
		{
			this.headYRot = headYRot;
			this.headXRot = headXRot;
		}
		
		@Override
		public float partialTick()
		{
			return this.partialTick;
		}
		
		@Override
		public @Nullable PBakedModel getBakedModel()
		{
			return this.model;
		}
		
		@Override
		public T getAnimatable()
		{
			return this.animatable;
		}
		
		@Override
		public AnimManagerKey getAnimKey()
		{
			return this.key;
		}
		
		public float getHeadXRot()
		{
			return this.headXRot;
		}
		
		public float getHeadYRot()
		{
			return this.headYRot;
		}
	}
}
