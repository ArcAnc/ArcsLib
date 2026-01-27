/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.content.block.block_entity;


import com.arcanc.arcslib.api.ArcAnimatable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TestBlockEntity extends BlockEntity implements ArcAnimatable
{
	public TestBlockEntity(BlockPos pos, BlockState blockState)
	{
		super(com.arcanc.arcslib.content.registration.Registration.BETypeReg.TEST_BLOCK_ENTITY.get(), pos, blockState);
	}
}
