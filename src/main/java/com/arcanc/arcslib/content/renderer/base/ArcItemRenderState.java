/**
 * @author ArcAnc
 * Created at: 28.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.content.renderer.base;


import com.arcanc.arcslib.content.animatable.ArcAnimatable;
import com.arcanc.arcslib.content.model.baked.ArcBakedModel;
import com.arcanc.arcslib.content.renderer.ArcItemRenderer;
import com.arcanc.arcslib.util.helpers.RenderHelper;
import net.minecraft.client.gui.render.state.GuiItemRenderState;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

public interface ArcItemRenderState<T extends Item & ArcAnimatable<T>> extends ArcRenderState<T>
{
	<RS extends ArcItemRenderState<T>> void extractStackData(
			ItemStack stack,
			ArcItemRenderer<T, RS> renderer);
	
	@ApiStatus.Internal
	void extractAdditionalData(ItemDisplayContext context, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor);
	
	@ApiStatus.Internal
	void extractGuiItemRenderState(GuiItemRenderState renderState);
	
	ItemDisplayContext context();
	int lightCoords();
	int overlayCoords();
	boolean hasFoil();
	int outlineColor();
	
	GuiItemRenderState itemRenderState();
	
	class Impl<T extends Item & ArcAnimatable<T>> implements ArcItemRenderState<T>
	{
		private ArcBakedModel model;
		private T animatable;
		private float partialTicks;
		private ItemDisplayContext context;
		private int lightCoords;
		private int overlayCoords;
		private boolean hasFoil;
		private int outlineColor;
		private GuiItemRenderState guiItemRenderState;
		
		@Override
		public void extractData()
		{
			this.partialTicks = RenderHelper.mc().getDeltaTracker().getGameTimeDeltaPartialTick(false);
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
		
		@Override
		public <RS extends ArcItemRenderState<T>> void extractStackData(ItemStack stack, ArcItemRenderer<T, RS> renderer)
		{
			this.extractData();
			this.model = renderer.getArcModel();
			//TODO: remove this hack
			@SuppressWarnings ("unchecked") T item = (T) stack.getItem();
			this.animatable = item;
		}
		
		@Override
		public void extractAdditionalData(ItemDisplayContext context, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor)
		{
			this.context = context;
			this.lightCoords = lightCoords;
			this.overlayCoords = overlayCoords;
			this.hasFoil = hasFoil;
			this.outlineColor = outlineColor;
		}
		
		@Override
		public void extractGuiItemRenderState(GuiItemRenderState renderState)
		{
			this.guiItemRenderState = renderState;
		}
		
		@Override
		public ItemDisplayContext context()
		{
			return this.context;
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
		public GuiItemRenderState itemRenderState()
		{
			return this.guiItemRenderState;
		}
		
	}
}
