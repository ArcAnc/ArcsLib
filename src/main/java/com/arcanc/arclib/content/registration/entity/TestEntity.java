/**
 * @author ArcAnc
 * Created at: 27.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arclib.content.registration.entity;


import com.arcanc.arclib.content.animatable.ArcAnimatable;
import com.arcanc.arclib.content.animatable.ArcAnimationManager;
import com.arcanc.arclib.content.animatable.instance.ArcAnimationController;
import com.arcanc.arclib.content.animatable.instance.ControllerState;
import com.arcanc.arclib.content.model.animation.ArcRawAnimation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class TestEntity extends Entity implements ArcAnimatable<TestEntity>
{
	private static final ArcRawAnimation ANIMATION = ArcRawAnimation.begin().
			thenPlay("animation").
			thenWait(20).
			thenLoop("animation").
			build();
	private final ArcAnimationManager<TestEntity> animationManager = new ArcAnimationManager<>(this);
	
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
	public ArcAnimationManager<TestEntity> getAnimationManager()
	{
		return this.animationManager;
	}
	
	@Override
	public void registerAnimationControllers(ArcAnimationManager.@NotNull ArcAnimationRegistrar<TestEntity> registrar)
	{
		registrar.add(new ArcAnimationController<>(state ->
		{
			state.controller().play(ANIMATION);
			return ControllerState.PLAY;
		}));
	}
}
