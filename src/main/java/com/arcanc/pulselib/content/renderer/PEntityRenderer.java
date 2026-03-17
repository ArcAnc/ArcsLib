/**
 * @author ArcAnc
 * Created at: 27.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.renderer;


import com.arcanc.pulselib.content.animatable.PAnimatable;
import com.arcanc.pulselib.content.animatable.instance.PAnimationController;
import com.arcanc.pulselib.content.model.animation.BoneFrame;
import com.arcanc.pulselib.content.model.baked.PBakedBone;
import com.arcanc.pulselib.content.model.baked.PBakedModel;
import com.arcanc.pulselib.content.renderer.modelData.PModelData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.Collection;
import java.util.function.Function;

public abstract class PEntityRenderer<T extends Entity & PAnimatable<T>> extends EntityRenderer<T>
	implements PRenderer<T>
{
	private final PModelData modelData;
	private final Function<ResourceLocation, RenderType> renderType;
	
	public PEntityRenderer(EntityRendererProvider.Context context, PModelData modelData, Function<ResourceLocation, RenderType> renderType)
	{
		super(context);
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
		return this.modelData.getModel();
	}
	
	@Override
	public RenderType getRenderType(ResourceLocation texture)
	{
		return this.renderType.apply(texture);
	}
	
	@Override
	public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight)
	{
		super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
		
		entity.getAnimationManager().getControllers().
				forEach(($, controller) -> controller.tick(entity, this.getModel(entity), partialTick));
		
		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(180));
		if (entity instanceof LivingEntity living)
			applyPositioning(living, poseStack, partialTick);
		preSubmit(poseStack, entity, this :: getRenderType, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, partialTick);
		trueSubmit(poseStack, entity, this :: getRenderType, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, partialTick);
		postSubmit(poseStack, entity, this :: getRenderType, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, partialTick);
		poseStack.popPose();
	}
	
	@Override
	public ResourceLocation getTextureLocation(T entity)
	{
		return null;
	}
	
	@Override
	public void preSubmit(PoseStack poseStack, T animatable, Function<ResourceLocation, RenderType> renderType, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float partialTick, @Nullable Object... additionalData)
	{
	
	}
	
	@Override
	public void trueSubmit(PoseStack poseStack, T animatable, Function<ResourceLocation, RenderType> renderType, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float partialTick, @Nullable Object... additionalData)
	{
		Collection<PAnimationController<T>> controllers = animatable.getAnimationManager().getControllers().values();
		PBakedModel model = this.getModel(animatable);
		if (model == null)
			return;
		model.bones().forEach(bone -> perBoneSubmit(animatable, poseStack, bone, controllers, renderType, -1, packedLight, packedOverlay, partialTick));
	}
	
	@Override
	public void postSubmit(PoseStack poseStack, T animatable, Function<ResourceLocation, RenderType> renderType, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float partialTick, @Nullable Object... additionalData)
	{
	
	}
	
	protected void perBoneSubmit(T animatable, PoseStack poseStack, PBakedBone bone, Collection<PAnimationController<T>> controllers, Function<ResourceLocation, RenderType> renderType, int packedColor, int packedLight, int packedOverlay, float partialTick)
	{
		PModelData data = this.getModelData(animatable);
		BoneFrame frame = bone.mixBone(data.getModel(), controllers);
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
			
			RenderType type = renderType.apply(modelData.getTextureByName(mesh.textureName()));
			
			PRenderQueue.submitEntityMesh(type, mesh.vertexBuffer(), new PRenderQueue.InstanceData(matrix4fstack, color, packedLight, packedOverlay));
		});
	}
	
	protected void applyPositioning(LivingEntity entity, PoseStack poseStack, float partialTick)
	{
		/// Vanilla copy paste
		boolean shouldSit = entity.isPassenger() && (entity.getVehicle() != null && entity.getVehicle().shouldRiderSit());
		float f = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
		float f1 = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
		float f2 = f1 - f;
		if (shouldSit && entity.getVehicle() instanceof LivingEntity livingentity)
		{
			f = Mth.rotLerp(partialTick, livingentity.yBodyRotO, livingentity.yBodyRot);
			f2 = f1 - f;
			float f7 = Mth.wrapDegrees(f2);
			if (f7 < -85.0F)
				f7 = -85.0F;
			
			if (f7 >= 85.0F)
				f7 = 85.0F;
			
			f = f1 - f7;
			if (f7 * f7 > 2500.0F)
				f += f7 * 0.2F;
			
			f2 = f1 - f;
		}
		
		float f6 = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
		if (LivingEntityRenderer.isEntityUpsideDown(entity))
		{
			f6 *= -1.0F;
			f2 *= -1.0F;
		}
		
		f2 = Mth.wrapDegrees(f2);
		if (entity.hasPose(Pose.SLEEPING))
		{
			Direction direction = entity.getBedOrientation();
			if (direction != null)
			{
				float f3 = entity.getEyeHeight(Pose.STANDING) - 0.1F;
				poseStack.translate((float)(-direction.getStepX()) * f3, 0.0F, (float)(-direction.getStepZ()) * f3);
			}
		}
		
		float f8 = entity.getScale();
		poseStack.scale(f8, f8, f8);
		float f9 = entity.tickCount + partialTick;
		this.setupRotations(entity, poseStack, f9, f, partialTick, f8);
	}
	
	protected void setupRotations(LivingEntity entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale)
	{
		if (entity.isFullyFrozen())
			yBodyRot += (float)(Math.cos((double)entity.tickCount * 3.25) * Math.PI * 0.4F);
		
		if (!entity.hasPose(Pose.SLEEPING))
			poseStack.mulPose(Axis.YN.rotationDegrees(yBodyRot));
		
		if (entity.deathTime > 0)
		{
			float f = ((float)entity.deathTime + partialTick - 1.0F) / 20.0F * 1.6F;
			f = Mth.sqrt(f);
			if (f > 1.0F)
				f = 1.0F;
			
			poseStack.mulPose(Axis.ZP.rotationDegrees(f * 90));
		}
		else if (entity.isAutoSpinAttack())
		{
			poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F - entity.getXRot()));
			poseStack.mulPose(Axis.YP.rotationDegrees(((float)entity.tickCount + partialTick) * -75.0F));
		}
		else if (entity.hasPose(Pose.SLEEPING))
		{
			Direction direction = entity.getBedOrientation();
			float f1 = direction != null ? sleepDirectionToRotation(direction) : yBodyRot;
			poseStack.mulPose(Axis.YP.rotationDegrees(f1));
			poseStack.mulPose(Axis.ZP.rotationDegrees(90));
			poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
		}
		else if (LivingEntityRenderer.isEntityUpsideDown(entity))
		{
			poseStack.translate(0.0F, (entity.getBbHeight() + 0.1F) / scale, 0.0F);
			poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
		}
	}
	
	private float sleepDirectionToRotation(Direction facing)
	{
		return switch (facing)
		{
			case SOUTH -> 90.0F;
			case NORTH -> 270.0F;
			case EAST -> 180.0F;
			default -> 0.0F;
		};
	}
}
