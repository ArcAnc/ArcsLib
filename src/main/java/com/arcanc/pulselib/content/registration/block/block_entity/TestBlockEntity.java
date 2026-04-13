/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.registration.block.block_entity;


import com.arcanc.pulselib.content.animatable.*;
import com.arcanc.pulselib.content.model.animation.PRawAnimation;
import com.arcanc.pulselib.util.helpers.PLibHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TestBlockEntity extends BlockEntity implements PAnimatable<TestBlockEntity>
{
	private final PAnimationManager<TestBlockEntity> animationManager = PLibHelper.createManager(this);
	private final PRawAnimation ANIMATION = PRawAnimation.begin().
			thenLoop("animation").
			build();
	private boolean playAnimation = true;
	
	public TestBlockEntity(BlockPos pos, BlockState blockState)
	{
		super(com.arcanc.pulselib.content.registration.Registration.BETypeReg.TEST_BLOCK_ENTITY.get(), pos, blockState);
	}
	
	public void changePlayAnimation()
	{
		this.playAnimation = !playAnimation;
		this.setChanged();
	}
	
	public boolean isPlayAnimation()
	{
		return this.playAnimation;
	}
	
	@Override
	public PAnimationManager<TestBlockEntity> getAnimationManager(AnimManagerKey key)
	{
		return this.animationManager;
	}
	
	@Override
	public void registerAnimationControllers(PAnimationManager.@NotNull PAnimationRegistrar<TestBlockEntity> registrar)
	{
		registrar.add(() -> animatableState ->
		{
			TestBlockEntity blockEntity = animatableState.animatable();
			PAnimationController<TestBlockEntity> controller = animatableState.controller();
			if (blockEntity.isPlayAnimation())
			{
				controller.play(ANIMATION);
				return ControllerState.PLAY;
			}
			else
			{
				controller.stop();
				return ControllerState.STOP;
			}
		});
	}
}
