/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arclib.content.registration.block.block_entity;


import com.arcanc.arclib.content.animatable.ArcAnimatable;
import com.arcanc.arclib.content.animatable.ArcAnimationManager;
import com.arcanc.arclib.content.animatable.instance.ArcAnimationController;
import com.arcanc.arclib.content.animatable.instance.ControllerState;
import com.arcanc.arclib.content.model.animation.ArcRawAnimation;
import com.arcanc.arclib.util.helpers.ArcLibHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

public class TestBlockEntity extends BlockEntity implements ArcAnimatable<TestBlockEntity>
{
	private final ArcAnimationManager<TestBlockEntity> animationManager = ArcLibHelper.createManager(this);
	private final ArcRawAnimation ANIMATION = ArcRawAnimation.begin().
			thenPlay("animation").
			thenWait(20).
			thenLoop("animation").
			build();
	private boolean playAnimation = false;
	
	public TestBlockEntity(BlockPos pos, BlockState blockState)
	{
		super(com.arcanc.arclib.content.registration.Registration.BETypeReg.TEST_BLOCK_ENTITY.get(), pos, blockState);
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
	public ArcAnimationManager<TestBlockEntity> getAnimationManager()
	{
		return this.animationManager;
	}
	
	@Override
	public void registerAnimationControllers(ArcAnimationManager.@NonNull ArcAnimationRegistrar<TestBlockEntity> registrar)
	{
		registrar.add(new ArcAnimationController<>(animatableState ->
		{
			TestBlockEntity blockEntity = animatableState.animatable();
			ArcAnimationController<TestBlockEntity> controller = animatableState.controller();
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
		}));
	}
}
