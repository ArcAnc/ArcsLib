/**
 * @author ArcAnc
 * Created at: 28.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.registration.item.renderer;


import com.arcanc.pulselib.content.registration.item.TestBlockItem;
import com.arcanc.pulselib.content.renderer.PItemRenderer;
import com.arcanc.pulselib.content.renderer.modelData.DefaultItemModelData;
import com.arcanc.pulselib.util.PLibDatabase;
import com.arcanc.pulselib.util.PRenderTypes;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;

public class TestBlockItemRenderer extends PItemRenderer<TestBlockItem>
{
	public static final ResourceLocation CIRCLE = PLibDatabase.rl("item/test_block/circle");
	public static final ResourceLocation PYRAMID = PLibDatabase.rl("item/test_block/pyramid");
	
	public TestBlockItemRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelSet entityModelSet)
	{
		super(new DefaultItemModelData.DefaultItemModelDataBuilder(PLibDatabase.rl("test_block")).
				build(),
				PRenderTypes.RenderTypeProvider :: trianglesSolid, blockEntityRenderDispatcher, entityModelSet);
	}
}
