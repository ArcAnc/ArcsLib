/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arclib.content.registration.block.block_entity.ber;


import com.arcanc.arclib.content.registration.block.block_entity.TestBlockEntity;
import com.arcanc.arclib.content.registration.block.block_entity.ber.renderState.TestBlockEntityRenderState;
import com.arcanc.arclib.content.renderer.ArcBlockRenderer;
import com.arcanc.arclib.content.renderer.modelData.DefaultBlockModelData;
import com.arcanc.arclib.util.Database;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class TestBlockEntityRenderer extends ArcBlockRenderer<TestBlockEntity, TestBlockEntityRenderState>
{
	public TestBlockEntityRenderer(final BlockEntityRendererProvider.Context ctx)
	{
		super(new DefaultBlockModelData.DefaultBlockModelDataBuilder(Database.rl("test_block")).
				addTexture(Database.rl("tube_texture")).
				addTexture(Database.rl("torus_texture")).
				addTexture(Database.rl("pyramid_texture")).
				addTexture(Database.rl("cube_texture")).
				build());
	}
	
	@Override
	public TestBlockEntityRenderState createRenderState()
	{
		return new TestBlockEntityRenderState();
	}
}
