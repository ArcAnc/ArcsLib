/**
 * @author ArcAnc
 * Created at: 02.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.util.armor;


import com.arcanc.pulselib.content.model.baked.PBakedBone;
import com.arcanc.pulselib.content.model.baked.PBakedModel;
import com.arcanc.pulselib.util.PRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;

public class PArmorLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M>
{
	public PArmorLayer(RenderLayerParent<T, M> parent)
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
		for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET))
		{
			ItemStack stack = entity.getItemBySlot(slot);
			
			PulseArmorModels.get(stack, slot).ifPresent(definition ->
					renderDefinition(poseStack, light, partialTick, slot, definition, attachment -> vanillaPart(attachment.vanillaPart())));
		}
	}
	
	public static void renderFirstPersonArm(PoseStack poseStack,
	                                        int light,
	                                        LivingEntity entity,
	                                        HumanoidArm arm,
	                                        ModelPart armPart,
	                                        float partialTick)
	{
		VanillaHumanoidPart targetPart = arm == HumanoidArm.RIGHT ? VanillaHumanoidPart.RIGHT_ARM : VanillaHumanoidPart.LEFT_ARM;
		
		float oldXRot = armPart.xRot;
		float oldYRot = armPart.yRot;
		float oldZRot = armPart.zRot;
		try
		{
			armPart.xRot = 0;
			armPart.yRot = 0;
			armPart.zRot = 0;
			
			for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET))
			{
				ItemStack stack = entity.getItemBySlot(slot);
				
				PulseArmorModels.get(stack, slot).ifPresent(definition ->
						renderDefinition(poseStack, light, partialTick, slot, definition,
								attachment -> attachment.vanillaPart() == targetPart ? armPart : null));
			}
		}
		finally
		{
			armPart.xRot = oldXRot;
			armPart.yRot = oldYRot;
			armPart.zRot = oldZRot;
		}
	}
	
	private static void renderDefinition(
			PoseStack poseStack,
			int light,
			float partialTick,
			EquipmentSlot slot,
			PulseArmorDefinition definition,
			AttachmentPartResolver partResolver)
	{
		PBakedModel model = definition.modelData().getModel();
		if (model == null)
			return;
			
		for (PulseArmorAttachment attachment : definition.attachments())
		{
			if (attachment.slot() != slot)
				continue;
			
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
			poseStack.translate(offset.x(), offset.y(), offset.z());
			poseStack.translate(-bone.basePosition().x(), -bone.basePosition().y(), -bone.basePosition().z());
			
			drawBone(poseStack, definition, bone, attachment, light, partialTick);
			
			poseStack.popPose();
		}
	}
	
	private static void drawBone(PoseStack poseStack,
	                             PulseArmorDefinition definition,
	                             PBakedBone bone,
	                             PulseArmorAttachment attachment,
	                             int light,
	                             float partialTick)
	{
		bone.instantDraw(
				poseStack,
				definition.modelData(),
				List.of(),
				PRenderTypes.RenderTypeProvider :: trianglesLit,
				attachment.color(),
				light,
				OverlayTexture.NO_OVERLAY,
				partialTick);
	}
	
	private ModelPart vanillaPart(VanillaHumanoidPart part)
	{
		HumanoidModel<T> model = this.getParentModel();
		return switch (part)
		{
			case HEAD -> model.head;
			case BODY -> model.body;
			case RIGHT_ARM -> model.rightArm;
			case LEFT_ARM -> model.leftArm;
			case RIGHT_LEG -> model.rightLeg;
			case LEFT_LEG -> model.leftLeg;
		};
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
		@Nullable ModelPart resolve(PulseArmorAttachment attachment);
	}
}
