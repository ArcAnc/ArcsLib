/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.content.block;


import com.arcanc.arcslib.content.block.block_entity.TestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

public class TestBlock extends Block implements EntityBlock
{
	public TestBlock(Properties props)
	{
		super(props);
	}
	
	@Override
	public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState)
	{
		return new TestBlockEntity(blockPos, blockState);
	}
	
	@Override
	protected RenderShape getRenderShape(BlockState state)
	{
		return RenderShape.INVISIBLE;
	}
}
