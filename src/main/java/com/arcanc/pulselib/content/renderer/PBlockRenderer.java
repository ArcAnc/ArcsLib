/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.renderer;


import com.arcanc.pulselib.content.animatable.AnimManagerKey;
import com.arcanc.pulselib.content.animatable.PAnimatable;
import com.arcanc.pulselib.content.animatable.PAnimationController;
import com.arcanc.pulselib.content.animatable.PAnimationManager;
import com.arcanc.pulselib.content.animatable.instance.InstanceAnimationManager;
import com.arcanc.pulselib.content.model.animation.BoneFrame;
import com.arcanc.pulselib.content.model.baked.PBakedBone;
import com.arcanc.pulselib.content.model.baked.PBakedModel;
import com.arcanc.pulselib.content.renderer.modelData.PModelData;
import com.arcanc.pulselib.util.PRenderTypes;
import com.arcanc.pulselib.util.PTextureCache;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.Collection;
import java.util.function.Function;

public abstract class PBlockRenderer<T extends BlockEntity & PAnimatable<T>>
		implements PRenderer<T>, BlockEntityRenderer<T>
{
	private final PModelData modelData;
	private final Function<ResourceLocation, RenderType> renderType;
	
	public PBlockRenderer(PModelData modelData, Function<ResourceLocation, RenderType> renderType)
	{
		this.modelData = modelData;
		this.renderType = renderType;
	}
	
	@Override
	public PModelData getModelData(T animatable)
	{
		return this.modelData;
	}
	
	@Override
	public @Nullable PBakedModel getModel(T animatable)
	{
		return getModelData(animatable).getModel();
	}
	
	@Override
	public RenderType getRenderType(ResourceLocation texture)
	{
		return this.renderType.apply(texture);
	}
	
	@Override
	public void render(T animatable, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay)
	{
		poseStack.pushPose();
		poseStack.translate(0.5f, 0, 0.5f);
		tryRotateToRealRotation(poseStack, getAnimatableFacing(animatable));
		preSubmit(poseStack, animatable, this :: getRenderType, bufferSource, packedLight, packedOverlay, partialTick);
		trueSubmit(poseStack, animatable, this :: getRenderType, bufferSource, packedLight, packedOverlay, partialTick);
		postSubmit(poseStack, animatable, this :: getRenderType, bufferSource, packedLight, packedOverlay, partialTick);
		poseStack.popPose();
	}
	
	@Override
	public void preSubmit(PoseStack poseStack, T animatable, Function<ResourceLocation, RenderType> renderType, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float partialTick, @Nullable Object... additionalData)
	{
	
	}
	
	@Override
	public void trueSubmit(PoseStack poseStack, T animatable, Function<ResourceLocation, RenderType> renderType, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float partialTick, @Nullable Object... additionalData)
	{
		PBakedModel model = this.getModelData(animatable).getModel();
		if (model == null)
			return;
		PAnimationManager<T> manager = animatable.getAnimationManager(AnimManagerKey.of(animatable));
		manager.bindModel(model);
		Collection<PAnimationController<T>> controllers = manager.getControllers().values();
		InstanceAnimationManager.addManager(manager);
		
		model.bones().forEach(bone -> perBoneSubmit(animatable, poseStack, bone, controllers, renderType, -1, packedLight, packedOverlay, partialTick));
	}
	
	@Override
	public void postSubmit(PoseStack poseStack, T animatable, Function<ResourceLocation, RenderType> renderType, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float partialTick, @Nullable Object... additionalData)
	{
	
	}
	
	protected void perBoneSubmit(T animatable, PoseStack poseStack, PBakedBone bone, Collection<PAnimationController<T>> controllers, Function<ResourceLocation, RenderType> renderType, int packedColor, int packedLight, int packedOverlay, float partialTick)
	{
		PModelData data = this.getModelData(animatable);
		BoneFrame frame = bone.mixBone(data.getModel(), controllers, partialTick);
		poseStack.pushPose();
		if (frame != null)
		{
			poseStack.translate(frame.translation().x(), frame.translation().y(), frame.translation().z());
			poseStack.mulPose(frame.rotation());
			poseStack.scale(frame.scale().x(), frame.scale().y(), frame.scale().z());
		}
		else
		{
			poseStack.translate(bone.basePosition().x(), bone.basePosition().y(), bone.basePosition().z());
			poseStack.mulPose(bone.baseRotation());
		}
		
		this.submitBone(animatable, bone, poseStack, data, controllers, renderType, packedColor, packedLight, packedOverlay, partialTick);
		
		if (!bone.children().isEmpty())
			bone.children().forEach(child -> perBoneSubmit(animatable, poseStack, child, controllers, renderType, packedColor, packedLight, packedOverlay, partialTick));

		poseStack.popPose();
	}
	
	private void tryRotateToRealRotation(PoseStack poseStack, Direction facing)
	{
		if (facing.getAxis().isHorizontal())
			poseStack.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
		else
			poseStack.mulPose(Axis.XP.rotationDegrees(90 * facing.getNormal().getY()));
	}
	
	private Direction getAnimatableFacing(T animatable)
	{
		BlockState blockState = animatable.getBlockState();
		Direction dir = Direction.NORTH;
		
		if (blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
			dir = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
		
		if (blockState.hasProperty(BlockStateProperties.FACING))
			dir = blockState.getValue(BlockStateProperties.FACING);
		
		if (dir.getAxis() ==  Direction.Axis.Z)
			dir = dir.getOpposite();
		return dir;
	}
	
	protected void submitBone(T animatable,
	                          PBakedBone bone,
	                          PoseStack poseStack,
	                          PModelData modelData,
	                          Collection<PAnimationController<T>> controllers,
	                          Function<ResourceLocation, RenderType> renderType,
	                          int color,
	                          int packedLight,
	                          int packedOverlay,
	                          float partialTick)
	{
		Matrix4f matrix4fstack = new Matrix4f(poseStack.last().pose());

		bone.meshes().forEach(mesh ->
		{
			if (mesh.textureName().isEmpty())
				return;
			
			RenderType type = renderType.apply(PTextureCache.ATLAS_LOCATION);
			
			PRenderTypes.getTransparencyState(type).ifPresent(transparency ->
			{
				if (transparency == RenderStateShard.TransparencyStateShard.NO_TRANSPARENCY)
					PRenderQueue.submitBlockEntityMesh(type, mesh.vertexBuffer(), new PRenderQueue.InstanceData(matrix4fstack, color, packedLight, packedOverlay));
				else
					PRenderQueue.submitBlockEntityTranslucentMesh(type, mesh.vertexBuffer(), new PRenderQueue.InstanceData(matrix4fstack, color, packedLight, packedOverlay));
			});
		});
	}
}
