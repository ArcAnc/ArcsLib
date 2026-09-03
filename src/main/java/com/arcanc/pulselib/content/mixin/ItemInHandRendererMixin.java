/**
 * @author ArcAnc
 * Created at: 30.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.mixin;

import com.arcanc.pulselib.content.player.animation.PPlayerAnimations;
import com.arcanc.pulselib.content.player.animation.PPlayerPart;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin
{
	@Inject(method = "submitArmWithItem", at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",
			shift = At.Shift.AFTER))
	private void pulselib$renderAnimatedArmWithItem(AbstractClientPlayer player,
	                                                float partialTick,
	                                                float pitch,
	                                                InteractionHand hand,
	                                                float swingProgress,
	                                                ItemStack stack,
	                                                float equippedProgress,
	                                                PoseStack poseStack,
	                                                SubmitNodeCollector submitNodeCollector,
	                                                int packedLight,
	                                                CallbackInfo ci)
	{
		if (stack.isEmpty() || stack.getItem() instanceof MapItem)
			return;

		HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
		PPlayerPart playerPart = arm == HumanoidArm.RIGHT ? PPlayerPart.RIGHT_ARM : PPlayerPart.LEFT_ARM;
		if (!PPlayerAnimations.isPartAnimating(player, playerPart, partialTick))
			return;
		poseStack.pushPose();
		try
		{
			((ItemInHandRendererAccessor)this).pulselib$renderPlayerArm(
					poseStack,
					submitNodeCollector,
					packedLight,
					equippedProgress,
					swingProgress,
					arm);
		}
		finally
		{
			poseStack.popPose();
		}
	}

	@ModifyVariable (method = "submitArmWithItem", at = @At (value = "STORE"), name = "isMainHand")
	private boolean pulselib$renderAnimatedOffHand(boolean renderMainHand,
	                                               AbstractClientPlayer player,
	                                               float partialTick,
	                                               float pitch,
	                                               InteractionHand hand,
	                                               float swingProgress,
	                                               ItemStack stack)
	{
		if (renderMainHand || hand != InteractionHand.OFF_HAND || !stack.isEmpty())
			return renderMainHand;

		PPlayerPart offHandPart = player.getMainArm() == HumanoidArm.RIGHT ? PPlayerPart.LEFT_ARM : PPlayerPart.RIGHT_ARM;
		return PPlayerAnimations.isPartAnimating(player, offHandPart, partialTick);
	}

	@ModifyVariable (method = "submitArmWithItem", at = @At (value = "STORE"), name = "arm")
	private HumanoidArm pulselib$keepPhysicalArm(HumanoidArm arm,
	                                             AbstractClientPlayer player,
	                                             float partialTick,
	                                             float pitch,
	                                             InteractionHand hand)
	{
		return hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
	}
}
