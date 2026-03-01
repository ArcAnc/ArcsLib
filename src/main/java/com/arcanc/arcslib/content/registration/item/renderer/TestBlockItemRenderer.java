/**
 * @author ArcAnc
 * Created at: 28.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.content.registration.item.renderer;


import com.arcanc.arcslib.content.registration.item.TestBlockItem;
import com.arcanc.arcslib.content.registration.item.renderer.renderState.TestBlockItemRenderState;
import com.arcanc.arcslib.content.renderer.ArcItemRenderer;
import com.arcanc.arcslib.content.renderer.modelData.ArcModelData;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import org.jspecify.annotations.NonNull;

public class TestBlockItemRenderer extends ArcItemRenderer<TestBlockItem, TestBlockItemRenderState>
{
	public TestBlockItemRenderer(ArcModelData modelData)
	{
		super(modelData);
	}
	
	@Override
	protected TestBlockItemRenderState createRenderState()
	{
		return new TestBlockItemRenderState();
	}
	
	public record Unbaked(ArcModelData data) implements SpecialModelRenderer.Unbaked
	{
		public static final MapCodec<Unbaked> MAP_CODEC = ArcModelData.CODEC.
				xmap(Unbaked :: new, Unbaked :: data);
		
		@Override
		public @NonNull SpecialModelRenderer<?> bake(BakingContext context)
		{
			return new TestBlockItemRenderer(this.data);
		}
		
		@Override
		public @NonNull MapCodec<Unbaked> type()
		{
			return MAP_CODEC;
		}
	}
}
