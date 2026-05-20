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
import com.arcanc.pulselib.content.animatable.PAnimationController;
import com.arcanc.pulselib.content.animatable.PAnimationManager;
import com.arcanc.pulselib.content.animatable.instance.InstanceAnimationManager;
import com.arcanc.pulselib.content.model.animation.BoneFrame;
import com.arcanc.pulselib.content.model.baked.PBakedBone;
import com.arcanc.pulselib.content.model.baked.PBakedModel;
import com.arcanc.pulselib.content.renderer.base.PEntityRenderState;
import com.arcanc.pulselib.content.renderer.modelData.PModelData;
import com.arcanc.pulselib.util.PTextureCache;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class PEntityRenderer<T extends Entity & PAnimatable<T>, RS extends EntityRenderState & PEntityRenderState<T>> extends EntityRenderer<T, RS>
	implements PRenderer<T, RS>
{
	private final PModelData modelData;
	private final Function<Identifier, RenderType> renderType;
	private final Map<String, List<PEntityRenderLayer<T, RS>>> renderLayers = new Object2ObjectOpenHashMap<>();
	
	public PEntityRenderer(EntityRendererProvider.Context context, PModelData modelData, Function<Identifier, RenderType> renderType)
	{
		super(context);
		this.modelData = modelData;
		this.renderType = renderType;
	}
	
	@Override
	public PModelData getModelData(RS renderState)
	{
		return this.modelData;
	}
	
	@Override
	public @Nullable PBakedModel getModel(RS renderState)
	{
		return getModelData(renderState).getModel();
	}
	
	@Override
	public RenderType getRenderType(Identifier texture)
	{
		return this.renderType.apply(texture);
	}
	
	public void addRenderLayer(String boneName, PEntityRenderLayer<T, RS> renderLayer)
	{
		this.renderLayers.compute(boneName, (bone, listLayers) ->
		{
			if (listLayers == null)
				listLayers = new ArrayList<>();
			listLayers.add(renderLayer);
			return listLayers;
		});
	}
	
	@Override
	public void extractRenderState(T entity, RS renderState, float partialTick)
	{
		super.extractRenderState(entity, renderState, partialTick);
		renderState.extractEntityData(entity, this);
		if (renderState.getAnimatable() instanceof LivingEntity living && renderState instanceof PEntityRenderState.LivingImpl<?> livingEntityRenderState)
			extractLivingRenderData(living, livingEntityRenderState, partialTick);
	}
	
	@Override
	public void submit(RS renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState)
	{
		super.submit(renderState, poseStack, submitNodeCollector, cameraRenderState);
		
		preSubmit(poseStack, renderState, cameraRenderState, submitNodeCollector);
		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(180));
		trueSubmit(poseStack, renderState, cameraRenderState, submitNodeCollector);
		poseStack.popPose();
		postSubmit(poseStack, renderState, cameraRenderState, submitNodeCollector);
	}
	
	@Override
	public void preSubmit(PoseStack poseStack, RS renderState, CameraRenderState cameraRenderState, SubmitNodeCollector submitNodeCollector)
	{
	
	}
	
	@Override
	public void trueSubmit(PoseStack poseStack, RS renderState, CameraRenderState cameraRenderState, SubmitNodeCollector submitNodeCollector)
	{
		PBakedModel model = renderState.getBakedModel();
		if (model == null)
			return;
		
		PAnimationManager<T> manager = renderState.getAnimatable().getAnimationManager(renderState.getAnimKey());
		manager.bindModel(model);
		InstanceAnimationManager.addManager(manager);
		
		Collection<PAnimationController<T>> controllers = manager.getControllers().values();
		int packedOverlay;
		
		if (renderState instanceof LivingEntityRenderState livingEntityRenderState)
		{
			packedOverlay = LivingEntityRenderer.getOverlayCoords(livingEntityRenderState, 0.0f);
			float scale = livingEntityRenderState.scale;
			poseStack.scale(scale, scale, scale);
			setupRotations(livingEntityRenderState, poseStack, livingEntityRenderState.bodyRot, scale);
		}
		else
		{
			packedOverlay = OverlayTexture.NO_OVERLAY;
		}
		
		for (PBakedBone bone : model.bones())
			perBoneSubmit(renderState, poseStack, bone, controllers, this.getModelData(renderState), renderType, -1, renderState.lightCoords, packedOverlay, renderState.partialTick(), submitNodeCollector, cameraRenderState, null);
	}
	
	@Override
	public void postSubmit(PoseStack poseStack, RS renderState, CameraRenderState cameraRenderState, SubmitNodeCollector submitNodeCollector)
	{
	
	}
	
	protected void perBoneSubmit(RS renderState,
	                             PoseStack poseStack,
	                             PBakedBone bone,
	                             Collection<PAnimationController<T>> controllers,
	                             PModelData data,
	                             Function<Identifier, RenderType> renderType,
	                             int packedColor,
	                             int packedLight,
	                             int packedOverlay,
	                             float partialTick,
	                             SubmitNodeCollector submitNodeCollector,
	                             CameraRenderState cameraRenderState,
	                             @Nullable PEntityRenderLayer<T, RS> renderLayer)
	{
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
		if (bone.name().equals("head") && renderState instanceof PEntityRenderState.LivingImpl<?> livingEntityRenderState)
		{
			poseStack.mulPose(Axis.YN.rotationDegrees(livingEntityRenderState.getHeadYRot()));
			poseStack.mulPose(Axis.XN.rotationDegrees(livingEntityRenderState.getHeadXRot()));
		}
		if (renderLayer == null)
		{
			this.submitBone(renderState, bone, poseStack, data, controllers, renderType, packedColor, packedLight, packedOverlay, partialTick);
			List<PEntityRenderLayer<T, RS>> layers = this.renderLayers.get(bone.name());
			if (layers != null)
				for (PEntityRenderLayer<T, RS> layer : layers)
					if (layer.shouldRender(renderState))
					{
						poseStack.pushPose();
						Vector3f offset = layer.offset();
						poseStack.translate(offset.x(), offset.y(), offset.z());
						poseStack.mulPose(layer.rotation());
						Vector3f scale = layer.scale();
						poseStack.scale(scale.x(), scale.y(), scale.z());
						layer.submit(this, renderState, poseStack, submitNodeCollector, cameraRenderState, controllers, packedColor, packedLight, packedOverlay, partialTick);
						poseStack.popPose();
					}
		}
		else
			this.submitBone(renderState, bone, poseStack, data, controllers, renderType, packedColor, packedLight, packedOverlay, partialTick, renderLayer);
		
		if (!bone.children().isEmpty())
			bone.children().forEach(child -> perBoneSubmit(renderState, poseStack, child, controllers, data, renderType, packedColor, packedLight, packedOverlay, partialTick, submitNodeCollector, cameraRenderState, renderLayer));
		
		poseStack.popPose();
	}
	
	protected void submitBone(RS renderState,
	                          PBakedBone bone,
	                          PoseStack poseStack,
	                          PModelData modelData,
	                          Collection<PAnimationController<T>> controllers,
	                          Function<Identifier, RenderType> renderType,
	                          int color,
	                          int packedLight,
	                          int packedOverlay,
	                          float partialTick)
	{
		submitBone(renderState, bone, poseStack, modelData, controllers, renderType, color, packedLight, packedOverlay, partialTick, null);
	}
	
	protected void submitBone(RS renderState,
	                          PBakedBone bone,
	                          PoseStack poseStack,
	                          PModelData modelData,
	                          Collection<PAnimationController<T>> controllers,
	                          Function<Identifier, RenderType> renderType,
	                          int color,
	                          int packedLight,
	                          int packedOverlay,
	                          float partialTick,
	                          @Nullable PEntityRenderLayer<T, RS> renderLayer)
	{
		Matrix4f matrix4fstack = new Matrix4f(poseStack.last().pose());
		
		bone.meshes().forEach(mesh ->
		{
			if (mesh.textureName().isEmpty())
				return;
			
			RenderType type = renderType.apply(PTextureCache.ATLAS_LOCATION);
			int packedColor = renderLayer == null ? color : renderLayer.getColor(renderState, bone, mesh, color);
			
			PRenderQueue.submitEntityMesh(type, mesh, new PRenderQueue.InstanceData(matrix4fstack, packedColor, packedLight, packedOverlay));
		});
	}
	
	protected void setupRotations(LivingEntityRenderState renderState, PoseStack poseStack, float yBodyRot, float scale)
	{
		if (renderState.isFullyFrozen)
			yBodyRot += (float)(Math.cos(Mth.floor(renderState.ageInTicks) * 3.25F) * Math.PI * 0.4F);
		
		if (!renderState.hasPose(Pose.SLEEPING))
			poseStack.mulPose(Axis.YN.rotationDegrees(yBodyRot));
		
		float deathTime = renderState.deathTime;
		if (deathTime > 0)
		{
			float f = (deathTime - 1.0F) / 20.0F * 1.6F;
			f = Mth.sqrt(f);
			if (f > 1.0F)
				f = 1.0F;
			
			poseStack.mulPose(Axis.ZP.rotationDegrees(f * 90));
		}
		else if (renderState.isAutoSpinAttack)
		{
			poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F - renderState.xRot));
			poseStack.mulPose(Axis.YP.rotationDegrees(renderState.ageInTicks * -75.0F));
		}
		else if (renderState.hasPose(Pose.SLEEPING))
		{
			Direction direction = renderState.bedOrientation;
			float f1 = direction != null ? sleepDirectionToRotation(direction) : yBodyRot;
			poseStack.mulPose(Axis.YP.rotationDegrees(f1));
			poseStack.mulPose(Axis.ZP.rotationDegrees(90));
			poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
		}
		else if (renderState.isUpsideDown)
		{
			poseStack.translate(0.0F, (renderState.boundingBoxHeight + 0.1F) / scale, 0.0F);
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
	
	protected void extractLivingRenderData(LivingEntity entity, PEntityRenderState.LivingImpl<?> state, float partialTick)
	{
		// Vanilla md copy/paste
		float headYRot = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
		float headXRot = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
		state.bodyRot = solveBodyRot(entity, headYRot, partialTick);
		state.yRot = Mth.wrapDegrees(headYRot - state.bodyRot);
		state.extractHeadData(state.yRot, headXRot);
		state.xRot = entity.getXRot(partialTick);
		state.isUpsideDown = this.isEntityUpsideDown(entity);
		
		if (state.isUpsideDown)
		{
			state.yRot *= -1f;
			state.xRot *= -1f;
		}
		
		float walkAnimationPos = 0.0f;
		float walkAnimationSpeed = 0.0f;
		
		if (!entity.isPassenger() && entity.isAlive())
		{
			walkAnimationPos = entity.walkAnimation.position(partialTick);
			walkAnimationSpeed = entity.walkAnimation.speed(partialTick);
		}
		
		state.walkAnimationPos = walkAnimationPos;
		state.walkAnimationSpeed = walkAnimationSpeed;
		
		float wornHeadAnimationPos = walkAnimationPos;
		
		if (entity.getVehicle() instanceof LivingEntity vehicle)
			wornHeadAnimationPos = vehicle.walkAnimation.position(partialTick);
		
		state.wornHeadAnimationPos = wornHeadAnimationPos;
		
		state.scale = entity.getScale();
		state.ageScale = entity.getAgeScale();
		state.pose = entity.getPose();
		
		state.bedOrientation = entity.getBedOrientation();
		
		if (state.bedOrientation != null)
			state.eyeHeight = entity.getEyeHeight(Pose.STANDING);
		
		state.isFullyFrozen = entity.isFullyFrozen();
		state.isBaby = entity.isBaby();
		state.isInWater = entity.isInWater();
		state.isAutoSpinAttack = entity.isAutoSpinAttack();
		state.ticksSinceKineticHitFeedback = entity.getTicksSinceLastKineticHitFeedback(partialTick);
		state.hasRedOverlay = entity.hurtTime > 0 || entity.deathTime > 0;
		ItemStack headItem = entity.getItemBySlot(EquipmentSlot.HEAD);
		
		SkullBlock.Type wornHeadType = null;
		ResolvableProfile wornHeadProfile = null;
		
		if (headItem.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof AbstractSkullBlock skullBlock)
		{
			wornHeadType = skullBlock.getType();
			wornHeadProfile = headItem.get(DataComponents.PROFILE);
			state.headItem.clear();
		}
		else
		{
			// TODO: add hat rendering
			/*if (!HumanoidArmorLayer.shouldRender(headItem, EquipmentSlot.HEAD))
				this.itemModelResolver.updateForLiving(state.headItem, headItem, ItemDisplayContext.HEAD, entity);
			else*/
			state.headItem.clear();
		}
		
		state.wornHeadType = wornHeadType;
		state.wornHeadProfile = wornHeadProfile;
		
		state.deathTime = entity.deathTime > 0 ? entity.deathTime + partialTick : 0.0f;
		Minecraft minecraft = Minecraft.getInstance();
		state.isInvisibleToPlayer = state.isInvisible && entity.isInvisibleTo(minecraft.player);
	}
	
	private float solveBodyRot(LivingEntity entity, float headRot, float partialTick)
	{
		if (entity.getVehicle() instanceof LivingEntity riding)
		{
			float bodyRot = Mth.rotLerp(partialTick, riding.yBodyRotO, riding.yBodyRot);
			float maxHeadDiff = 85.0F;
			float headDiff = Mth.clamp(Mth.wrapDegrees(headRot - bodyRot), -maxHeadDiff, maxHeadDiff);
			bodyRot = headRot - headDiff;
			if (Math.abs(headDiff) > 50.0F)
				bodyRot += headDiff * 0.2F;
			
			return bodyRot;
		}
		else
			return Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
	}
	
	protected boolean isEntityUpsideDown(LivingEntity mob)
	{
		Component customName = mob.getCustomName();
		return customName != null && isUpsideDownName(customName.getString());
	}
	
	protected static boolean isUpsideDownName(String name)
	{
		return "Dinnerbone".equals(name) || "Grumm".equals(name);
	}
}
