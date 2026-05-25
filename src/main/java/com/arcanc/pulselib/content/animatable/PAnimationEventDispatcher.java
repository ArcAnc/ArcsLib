/**
 * @author ArcAnc
 * Created at: 25.05.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.animatable;


import com.arcanc.pulselib.content.model.animation.PAnimationEvent;
import com.arcanc.pulselib.util.PLibDatabase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Vector3f;

public class PAnimationEventDispatcher
{
	public static <T extends PAnimatable<T>> void dispatch(T animatable, PAnimationEvent event)
	{
		PositionContext position = position(animatable);
		if (position == null || !position.level().isClientSide())
			return;
		
		event.dispatch(position);
	}
	
	private static PositionContext position(PAnimatable<?> animatable)
	{
		if (animatable instanceof Entity entity)
			return new PositionContext(entity.level(), entity.getX(), entity.getY(), entity.getZ());
		
		if (animatable instanceof BlockEntity blockEntity && blockEntity.getLevel() != null)
		{
			BlockPos pos = blockEntity.getBlockPos();
			return new PositionContext(
					blockEntity.getLevel(),
					pos.getX() + 0.5,
					pos.getY() + 0.5,
					pos.getZ() + 0.5);
		}
		
		return null;
	}
	
	public record PositionContext(Level level, double x, double y, double z)
	{
	}
}
