/**
 * @author ArcAnc
 * Created at: 28.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arclib.content.registration.item.renderer;


import com.arcanc.arclib.content.registration.item.TestBlockItem;
import com.arcanc.arclib.content.renderer.ArcItemRenderer;
import com.arcanc.arclib.content.renderer.modelData.ArcModelData;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;

public class TestBlockItemRenderer extends ArcItemRenderer<TestBlockItem>
{
	public TestBlockItemRenderer(ArcModelData modelData, BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelSet entityModelSet)
	{
		super(modelData, blockEntityRenderDispatcher, entityModelSet);
	}
}
