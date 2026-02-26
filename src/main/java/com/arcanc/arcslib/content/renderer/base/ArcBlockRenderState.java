/**
 * @author ArcAnc
 * Created at: 25.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.content.renderer.base;


import com.arcanc.arcslib.api.ArcBlockRenderer;
import com.arcanc.arcslib.content.animatable.ArcAnimatable;
import com.arcanc.arcslib.content.model.baked.ArcBakedModel;
import com.arcanc.arcslib.util.helpers.RenderHelper;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.NonNull;

public interface ArcBlockRenderState<T extends BlockEntity & ArcAnimatable<T>> extends ArcRenderState<T>
{
	<RS extends BlockEntityRenderState & ArcBlockRenderState<T>> void extractBlockData(
			T blockEntity,
			@NonNull ArcBlockRenderer<T, RS> renderer,
			ModelFeatureRenderer.CrumblingOverlay breakProgress);
	
	class Impl<T extends BlockEntity & ArcAnimatable<T>> extends BlockEntityRenderState implements ArcBlockRenderState<T>
	{
		private float partialTicks;
		private ArcBakedModel model;
		private T animatable;
		
		@Override
		public void extractData()
		{
			this.partialTicks = RenderHelper.mc().getDeltaTracker().getGameTimeDeltaPartialTick(false);
		}
		
		public <RS extends BlockEntityRenderState & ArcBlockRenderState<T>> void extractBlockData(T blockEntity, @NonNull ArcBlockRenderer<T, RS> renderer, ModelFeatureRenderer.CrumblingOverlay breakProgress)
		{
			this.extractData();
			BlockEntityRenderState.extractBase(blockEntity, this, breakProgress);
			this.model = renderer.getArcModel();
			this.animatable = blockEntity;
		}

		
		@Override
		public float getPartialTicks()
		{
			return this.partialTicks;
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
