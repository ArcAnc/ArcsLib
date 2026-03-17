/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.registration.block.block_entity.ber;


import com.arcanc.pulselib.content.registration.block.block_entity.TestBlockEntity;
import com.arcanc.pulselib.content.renderer.PBlockRenderer;
import com.arcanc.pulselib.content.renderer.modelData.DefaultBlockModelData;
import com.arcanc.pulselib.util.Database;
import com.arcanc.pulselib.util.PRenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class TestBlockEntityRenderer extends PBlockRenderer<TestBlockEntity>
{
	public TestBlockEntityRenderer(final BlockEntityRendererProvider.Context ctx)
	{
		super(new DefaultBlockModelData.DefaultBlockModelDataBuilder(Database.rl("test_block")).
				addTexture(Database.rl("tube_texture")).
				addTexture(Database.rl("torus_texture")).
				addTexture(Database.rl("pyramid_texture")).
				addTexture(Database.rl("cube_texture")).
				build(),
				PRenderTypes.RenderTypeProvider :: trianglesSolid);
	}

}
