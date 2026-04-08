/**
 * @author ArcAnc
 * Created at: 28.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.renderer.base;


import com.arcanc.pulselib.content.animatable.AnimManagerKey;
import com.arcanc.pulselib.content.animatable.PAnimatable;
import com.arcanc.pulselib.content.model.baked.PBakedModel;
import com.arcanc.pulselib.content.renderer.PItemRenderer;
import com.arcanc.pulselib.util.helpers.PLibRenderHelper;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public interface PItemRenderState<T extends Item & PAnimatable<T>> extends PRenderState<T>
{
	<RS extends PItemRenderState<T>> void extractStackData(
			ItemStack stack,
			PItemRenderer<T, RS> renderer);
	
	@ApiStatus.Internal
	void extractAdditionalData(int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor);
	
	@ApiStatus.Internal
	void extractItemRenderState(ItemStackRenderState renderState);
	
	int lightCoords();
	int overlayCoords();
	boolean hasFoil();
	int outlineColor();
	
	ItemStackRenderState itemRenderState();
	
	class Impl<T extends Item & PAnimatable<T>> implements PItemRenderState<T>
	{
		private @Nullable PBakedModel model;
		private T animatable;
		private float partialTicks;
		private int lightCoords;
		private int overlayCoords;
		private boolean hasFoil;
		private int outlineColor;
		private ItemStackRenderState guiItemRenderState;
		private AnimManagerKey key;
		
		@Override
		public void extractData()
		{
			this.partialTicks = PLibRenderHelper.mc().isPaused() ? 0 : PLibRenderHelper.mc().getDeltaTracker().getGameTimeDeltaPartialTick(true);
		}
		
		@Override
		public float partialTick()
		{
			return this.partialTicks;
		}
		
		@Override
		public PBakedModel getBakedModel()
		{
			return this.model;
		}
		
		@Override
		public T getAnimatable()
		{
			return this.animatable;
		}
		
		@SuppressWarnings ("unchecked")
		@Override
		public <RS extends PItemRenderState<T>> void extractStackData(ItemStack stack, PItemRenderer<T, RS> renderer)
		{
			this.extractData();
			this.model = renderer.getModel((RS) this);
			this.key = AnimManagerKey.of(stack);
			//TODO: remove this hack
			this.animatable = (T) stack.getItem();
		}
		
		@Override
		public void extractAdditionalData(int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor)
		{
			this.lightCoords = lightCoords;
			this.overlayCoords = overlayCoords;
			this.hasFoil = hasFoil;
			this.outlineColor = outlineColor;
		}
		
		@Override
		public void extractItemRenderState(ItemStackRenderState renderState)
		{
			this.guiItemRenderState = renderState;
		}
		
		@Override
		public int lightCoords()
		{
			return this.lightCoords;
		}
		
		@Override
		public int overlayCoords()
		{
			return this.overlayCoords;
		}
		
		@Override
		public boolean hasFoil()
		{
			return this.hasFoil;
		}
		
		@Override
		public int outlineColor()
		{
			return this.outlineColor;
		}
		
		@Override
		public ItemStackRenderState itemRenderState()
		{
			return this.guiItemRenderState;
		}
		
		@Override
		public AnimManagerKey getAnimKey()
		{
			return this.key;
		}
	}
}
