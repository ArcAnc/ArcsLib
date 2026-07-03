/**
 * @author ArcAnc
 * Created at: 04.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.util.armor;


import com.arcanc.pulselib.content.animatable.PAnimationController;
import com.arcanc.pulselib.content.model.baked.PBakedBone;
import com.arcanc.pulselib.content.model.baked.PBakedModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.List;

public class PLivingAttachmentLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M>
{
	protected static final List<EquipmentSlot> EQUIPMENT_SLOTS = List.of(
			EquipmentSlot.HEAD,
			EquipmentSlot.CHEST,
			EquipmentSlot.LEGS,
			EquipmentSlot.FEET,
			EquipmentSlot.BODY,
			EquipmentSlot.MAINHAND,
			EquipmentSlot.OFFHAND);
	
	public PLivingAttachmentLayer(RenderLayerParent<T, M> parent)
	{
		super(parent);
	}
	
	@Override
	public void render(PoseStack poseStack,
	                   MultiBufferSource buffer,
	                   int light,
	                   T entity,
	                   float limbSwing,
	                   float limbSwingAmount,
	                   float partialTick,
	                   float ageInTicks,
	                   float netHeadYaw,
	                   float headPitch)
	{
		for (EquipmentSlot slot : EQUIPMENT_SLOTS)
		{
			ItemStack stack = entity.getItemBySlot(slot);
			for (PulseLivingDefinition definition : PulseLivingAttachments.get(stack, slot, entity))
				renderDefinition(poseStack, light, partialTick, entity, stack, definition,
						attachment -> PulseAttachmentAnchorResolvers.resolve(entity, this.getParentModel(), attachment.anchor()));
		}
		
		for (PulseLivingDefinition definition : PulseLivingAttachments.getGlobal(entity))
			renderDefinition(poseStack, light, partialTick, entity, ItemStack.EMPTY, definition,
					attachment -> PulseAttachmentAnchorResolvers.resolve(entity, this.getParentModel(), attachment.anchor()));
	}
	
	protected static void renderFirstPersonAnchor(PoseStack poseStack,
	                                              int light,
	                                              LivingEntity entity,
	                                              PulseAttachmentAnchor targetAnchor,
	                                              ModelPart anchorPart,
	                                              float partialTick)
	{
		float oldXRot = anchorPart.xRot;
		float oldYRot = anchorPart.yRot;
		float oldZRot = anchorPart.zRot;
		try
		{
			anchorPart.xRot = 0;
			anchorPart.yRot = 0;
			anchorPart.zRot = 0;
			
			for (EquipmentSlot slot : EQUIPMENT_SLOTS)
			{
				ItemStack stack = entity.getItemBySlot(slot);
				for (PulseLivingDefinition definition : PulseLivingAttachments.get(stack, slot, entity))
					renderDefinition(poseStack, light, partialTick, entity, stack, definition,
							attachment -> attachment.anchor().equals(targetAnchor) ? anchorPart : null);
			}
			
			for (PulseLivingDefinition definition : PulseLivingAttachments.getGlobal(entity))
				renderDefinition(poseStack, light, partialTick, entity, ItemStack.EMPTY, definition,
						attachment -> attachment.anchor().equals(targetAnchor) ? anchorPart : null);
		}
		finally
		{
			anchorPart.xRot = oldXRot;
			anchorPart.yRot = oldYRot;
			anchorPart.zRot = oldZRot;
		}
	}
	
	private static void renderDefinition(
			PoseStack poseStack,
			int light,
			float partialTick,
			LivingEntity entity,
			ItemStack stack,
			PulseLivingDefinition definition,
			AttachmentPartResolver partResolver)
	{
		PBakedModel model = definition.modelData().getModel();
		if (model == null)
			return;
		
		Collection<PAnimationController<?>> controllers = definition.animationControllers(entity, stack, model, partialTick);
		
		for (PulseLivingAttachment attachment : definition.attachments())
		{
			ModelPart vanillaPart = partResolver.resolve(attachment);
			if (vanillaPart == null)
				continue;
			
			PBakedBone bone = findBone(model.bones(), attachment.pulseBone());
			if (bone == null)
				continue;
			
			poseStack.pushPose();
			vanillaPart.translateAndRotate(poseStack);
			poseStack.mulPose(Axis.ZP.rotationDegrees(180));
			Vector3f offset = attachment.offset();
			Quaternionf rotation = attachment.rotation();
			Vector3f scale = attachment.scale();
			poseStack.translate(offset.x(), offset.y(), offset.z());
			poseStack.mulPose(rotation);
			poseStack.scale(scale.x(), scale.y(), scale.z());
			poseStack.translate(-bone.basePosition().x(), -bone.basePosition().y(), -bone.basePosition().z());
			
			drawBone(poseStack, definition, bone, attachment, controllers, light, partialTick);
			
			poseStack.popPose();
		}
	}
	
	private static void drawBone(PoseStack poseStack,
	                             PulseLivingDefinition definition,
	                             PBakedBone bone,
	                             PulseLivingAttachment attachment,
	                             Collection<PAnimationController<?>> controllers,
	                             int light,
	                             float partialTick)
	{
		drawBoneUnchecked(poseStack, definition, bone, attachment, controllers, light, partialTick);
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	private static void drawBoneUnchecked(PoseStack poseStack,
	                                      PulseLivingDefinition definition,
	                                      PBakedBone bone,
	                                      PulseLivingAttachment attachment,
	                                      Collection<PAnimationController<?>> controllers,
	                                      int light,
	                                      float partialTick)
	{
		bone.instantDraw(
				poseStack,
				definition.modelData(),
				(Collection) controllers,
				attachment.renderType(),
				attachment.color(),
				light,
				OverlayTexture.NO_OVERLAY,
				partialTick);
	}
	
	private static @Nullable PBakedBone findBone(List<PBakedBone> bones, String name)
	{
		for (PBakedBone bone : bones)
		{
			if (bone.name().equals(name))
				return bone;
			
			PBakedBone child = findBone(bone.children(), name);
			if (child != null)
				return child;
		}
		
		return null;
	}
	
	@FunctionalInterface
	private interface AttachmentPartResolver
	{
		@Nullable ModelPart resolve(PulseLivingAttachment attachment);
	}
}
