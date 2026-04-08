/**
 * @author ArcAnc
 * Created at: 25.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.renderer.base;


import com.arcanc.pulselib.content.animatable.AnimManagerKey;
import com.arcanc.pulselib.content.animatable.PAnimatable;
import com.arcanc.pulselib.content.model.baked.PBakedModel;
import com.arcanc.pulselib.content.renderer.PBlockRenderer;
import com.arcanc.pulselib.util.helpers.PLibRenderHelper;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

public interface PBlockRenderState<T extends BlockEntity & PAnimatable<T>> extends PRenderState<T>
{
	<RS extends BlockEntityRenderState & PBlockRenderState<T>> void extractBlockData(
			T blockEntity,
			PBlockRenderer<T, RS> renderer,
			ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress);
	
	class Impl<T extends BlockEntity & PAnimatable<T>> extends BlockEntityRenderState implements PBlockRenderState<T>
	{
		private float partialTicks;
		private @Nullable PBakedModel model;
		private T animatable;
		private AnimManagerKey key;
		
		@Override
		public void extractData()
		{
			this.partialTicks = PLibRenderHelper.mc().isPaused() ? 0 : PLibRenderHelper.mc().getDeltaTracker().getGameTimeDeltaPartialTick(true);
		}
		
		public <RS extends BlockEntityRenderState & PBlockRenderState<T>> void extractBlockData(T blockEntity, PBlockRenderer<T, RS> renderer, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress)
		{
			this.extractData();
			BlockEntityRenderState.extractBase(blockEntity, this, breakProgress);
			this.model = renderer.getModel((RS) this);
			this.animatable = blockEntity;
			this.key = AnimManagerKey.of(blockEntity);
		}
		
		@Override
		public float partialTick()
		{
			return this.partialTicks;
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
}
