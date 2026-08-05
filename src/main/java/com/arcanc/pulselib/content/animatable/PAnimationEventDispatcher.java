/**
 * @author ArcAnc
 * Created at: 27.05.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.animatable;


import com.arcanc.pulselib.content.model.animation.PAnimationEvent;
import com.arcanc.pulselib.content.model.animation.PModelPose;
import com.arcanc.pulselib.content.model.animation.PPose;
import com.arcanc.pulselib.content.model.baked.PBakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.Map;

public class PAnimationEventDispatcher
{
	public static <T extends PAnimatable<T>> void dispatch(T animatable, PAnimationEvent event)
	{
		dispatch(animatable, event, null, null);
	}
	
	public static <T extends PAnimatable<T>> void dispatch(T animatable,
	                                                       PAnimationEvent event,
	                                                       @Nullable PBakedModel model,
	                                                       @Nullable Collection<PAnimationController<T>> controllers)
	{
		PositionContext position = position(animatable);
		if (position == null || !position.level().isClientSide())
			return;
		
		if (model != null && controllers != null && !event.locator().isBlank())
			position = resolveLocatorPosition(animatable, event.locator(), model, controllers, position);
		
		event.dispatch(position);
	}
	
	private static <T extends PAnimatable<T>> PositionContext resolveLocatorPosition(T animatable,
	                                                                                 String locator,
	                                                                                 PBakedModel model,
	                                                                                 Collection<PAnimationController<T>> controllers,
	                                                                                 PositionContext fallback)
	{
		Vector3f localPosition = new Vector3f();
		if (!findBonePosition(model, controllers, locator, localPosition))
			return fallback;
		
		if (animatable instanceof Entity entity)
		{
			float scale = entity instanceof LivingEntity living ? living.getScale() : 1f;
			float yRot = entity instanceof LivingEntity living ? living.yBodyRot : entity.getYRot();
			localPosition.mul(scale);
			localPosition.rotateY((float) Math.toRadians(180f - yRot));
			return new PositionContext(
					fallback.level(),
					entity.getX() + localPosition.x(),
					entity.getY() + localPosition.y(),
					entity.getZ() + localPosition.z());
		}
		
		if (animatable instanceof BlockEntity blockEntity)
		{
			BlockPos pos = blockEntity.getBlockPos();
			rotateForBlockState(localPosition, blockEntity.getBlockState());
			return new PositionContext(
					fallback.level(),
					pos.getX() + 0.5 + localPosition.x(),
					pos.getY() + 0.5 + localPosition.y(),
					pos.getZ() + 0.5 + localPosition.z());
		}
		
		return fallback;
	}
	
	private static void rotateForBlockState(Vector3f localPosition, BlockState blockState)
	{
		Direction direction = Direction.NORTH;
		if (blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
			direction = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
		if (blockState.hasProperty(BlockStateProperties.FACING))
			direction = blockState.getValue(BlockStateProperties.FACING);
		
		if (direction.getAxis() == Direction.Axis.Z)
			direction = direction.getOpposite();
		
		if (direction.getAxis().isHorizontal())
			localPosition.rotateY((float) Math.toRadians(direction.toYRot()));
		else
			localPosition.rotateX((float) Math.toRadians(90f * direction.getNormal().getY()));
	}
	
	private static <T extends PAnimatable<T>> boolean findBonePosition(PBakedModel model,
	                                                                   Collection<PAnimationController<T>> controllers,
	                                                                   String boneName,
	                                                                   Vector3f result)
	{
		int boneIndex = model.boneIndex(boneName);
		if (boneIndex < 0)
			return false;
		PPose localPose = model.evaluate(controllers, Map.of(), 1f);
		PModelPose modelPose = new PModelPose(model.boneCount());
		modelPose.update(model, localPose);
		modelPose.transform(boneIndex).getTranslation(result);
		return true;
	}
	
	private static @Nullable PositionContext position(PAnimatable<?> animatable)
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
