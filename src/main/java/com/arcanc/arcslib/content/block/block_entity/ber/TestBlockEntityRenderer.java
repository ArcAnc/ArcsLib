/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.content.block.block_entity.ber;


import com.arcanc.arcslib.api.ArcBlockRenderer;
import com.arcanc.arcslib.api.ArcModelData;
import com.arcanc.arcslib.content.block.block_entity.TestBlockEntity;
import com.arcanc.arcslib.util.Database;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class TestBlockEntityRenderer extends ArcBlockRenderer<TestBlockEntity>
{
	public TestBlockEntityRenderer(final BlockEntityRendererProvider.Context ctx)
	{
		super(new ArcModelData(Database.rl("test_block"), "block",
				Database.rl("textures/block/test_block/tube_texture.png"),
				Database.rl("textures/block/test_block/torus_texture.png"),
				Database.rl("textures/block/test_block/pyramid_texture.png"),
				Database.rl("textures/block/test_block/cube_texture.png")));
	}
}
