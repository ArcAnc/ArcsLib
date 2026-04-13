/**
 * @author ArcAnc
 * Created at: 27.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.registration.entity;


import com.arcanc.pulselib.content.animatable.AnimManagerKey;
import com.arcanc.pulselib.content.animatable.ControllerState;
import com.arcanc.pulselib.content.animatable.PAnimatable;
import com.arcanc.pulselib.content.animatable.PAnimationManager;
import com.arcanc.pulselib.content.model.animation.PRawAnimation;
import com.arcanc.pulselib.util.helpers.PLibHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class TestEntity extends Mob implements PAnimatable<TestEntity>
{
	private static final PRawAnimation ANIMATION = PRawAnimation.begin().
			thenPlay("animation").
			build();
	private final PAnimationManager<TestEntity> animationManager = PLibHelper.createManager(this);
	
	public TestEntity(EntityType<? extends Mob> type, Level level)
	{
		super(type, level);
	}
	
	public static AttributeSupplier.Builder createAttributes()
	{
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.2F);
	}
	
	@Override
	public PAnimationManager<TestEntity> getAnimationManager(AnimManagerKey key)
	{
		return this.animationManager;
	}
	
	@Override
	public void registerAnimationControllers(PAnimationManager.@NotNull PAnimationRegistrar<TestEntity> registrar)
	{
		registrar.add(() -> state ->
		{
			state.controller().play(ANIMATION);
			return ControllerState.PLAY;
		});
	}
}
