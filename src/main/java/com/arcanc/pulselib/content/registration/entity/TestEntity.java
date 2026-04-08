/**
 * @author ArcAnc
 * Created at: 27.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.registration.entity;


import com.arcanc.pulselib.content.animatable.*;
import com.arcanc.pulselib.content.model.animation.PRawAnimation;
import com.arcanc.pulselib.util.helpers.PLibHelper;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;

public class TestEntity extends Entity implements PAnimatable<TestEntity>
{
	private static final PRawAnimation ANIMATION = PRawAnimation.begin().
			thenPlay("animation").
			thenWait(20).
			thenLoop("animation").
			build();
	private final PAnimationManager<TestEntity> animationManager = PLibHelper.createManager(this);
	
	public TestEntity(EntityType<?> type, Level level)
	{
		super(type, level);
	}
	
	@Override
	protected void defineSynchedData(SynchedEntityData.Builder entityData)
	{
	
	}
	
	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage)
	{
		return false;
	}
	
	@Override
	protected void readAdditionalSaveData(ValueInput input)
	{
	
	}
	
	@Override
	protected void addAdditionalSaveData(ValueOutput output)
	{
	
	}
	
	@Override
	public PAnimationManager<TestEntity> getAnimationManager(AnimManagerKey key)
	{
		return this.animationManager;
	}
	
	@Override
	public void registerAnimationControllers(PAnimationManager.@NonNull PAnimationRegistrar<TestEntity> registrar)
	{
		registrar.add(() -> new PAnimationController<>(state ->
		{
			state.controller().play(ANIMATION);
			return ControllerState.PLAY;
		}));
	}
}
