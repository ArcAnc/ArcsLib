/**
 * @author ArcAnc
 * Created at: 05.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.registration.item.renderer;


import com.arcanc.pulselib.content.registration.item.TestArmorItem;
import com.arcanc.pulselib.content.registration.item.renderer.renderState.TestArmorItemRenderState;
import com.arcanc.pulselib.content.renderer.PItemRenderer;
import com.arcanc.pulselib.content.renderer.modelData.PModelData;
import com.arcanc.pulselib.util.PRenderTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.special.SpecialModelRenderer;

public class TestArmorItemRenderer extends PItemRenderer<TestArmorItem, TestArmorItemRenderState>
{
	public TestArmorItemRenderer(PModelData modelData)
	{
		super(modelData, PRenderTypes.RenderTypeProvider :: trianglesSolid);
	}

	@Override
	protected TestArmorItemRenderState createRenderState()
	{
		return new TestArmorItemRenderState();
	}

	public record Unbaked(PModelData data) implements SpecialModelRenderer.Unbaked<TestArmorItemRenderState>
	{
		public static final MapCodec<Unbaked> MAP_CODEC = PModelData.CODEC.
				xmap(Unbaked :: new, Unbaked :: data);

		@Override
		public TestArmorItemRenderer bake(BakingContext context)
		{
			return new TestArmorItemRenderer(this.data);
		}

		@Override
		public MapCodec<Unbaked> type()
		{
			return MAP_CODEC;
		}
	}
}
