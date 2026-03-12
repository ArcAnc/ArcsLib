/**
 * @author ArcAnc
 * Created at: 27.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.registration.entity;


import com.arcanc.pulselib.content.animatable.PAnimatable;
import com.arcanc.pulselib.content.animatable.PAnimationManager;
import com.arcanc.pulselib.content.animatable.instance.PAnimationController;
import com.arcanc.pulselib.content.animatable.instance.ControllerState;
import com.arcanc.pulselib.content.model.animation.PRawAnimation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class TestEntity extends Entity implements PAnimatable<TestEntity>
{
	private static final PRawAnimation ANIMATION = PRawAnimation.begin().
			thenPlay("animation").
			thenWait(20).
			thenLoop("animation").
			build();
	private final PAnimationManager<TestEntity> animationManager = new PAnimationManager<>(this);
	
	public TestEntity(EntityType<?> type, Level level)
	{
		super(type, level);
	}
	
	@Override
	protected void defineSynchedData(SynchedEntityData.Builder entityData)
	{
	
	}
	
	@Override
	protected void readAdditionalSaveData(CompoundTag compound)
	{
	
	}
	
	@Override
	protected void addAdditionalSaveData(CompoundTag compound)
	{
	
	}
	
	@Override
	public PAnimationManager<TestEntity> getAnimationManager()
	{
		return this.animationManager;
	}
	
	@Override
	public void registerAnimationControllers(PAnimationManager.@NotNull PAnimationRegistrar<TestEntity> registrar)
	{
		registrar.add(new PAnimationController<>(state ->
		{
			state.controller().play(ANIMATION);
			return ControllerState.PLAY;
		}));
	}
}
