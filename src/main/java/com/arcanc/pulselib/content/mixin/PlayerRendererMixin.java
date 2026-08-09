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
import com.arcanc.pulselib.content.player.deformer.PPlayerMeshDeformers;
import com.arcanc.pulselib.util.attachments.humanoid.PHumanoidAttachmentLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.HumanoidArm;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.objectweb.asm.Opcodes;

import java.util.Set;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin
{
	@Unique private @Nullable PPlayerAnimations.PPlayerModelPose pulselib$armPose;

	@Inject(method = "renderHand", at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/model/geom/ModelPart;xRot:F",
			opcode = Opcodes.PUTFIELD,
			ordinal = 0,
			shift = At.Shift.AFTER))
	private void pulselib$applyArmAnimation(PoseStack poseStack,
	                                         MultiBufferSource bufferSource,
	                                         int packedLight,
	                                         AbstractClientPlayer player,
	                                         ModelPart arm,
	                                         ModelPart sleeve,
	                                         CallbackInfo ci)
	{
		PlayerModel<?> model = ((PlayerRenderer)(Object)this).getModel();
		sleeve.xRot = 0.0f;
		float partialTick = Minecraft.getInstance().isPaused() ? 0.0f : Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
		PPlayerPart playerPart = arm == model.rightArm ? PPlayerPart.RIGHT_ARM : PPlayerPart.LEFT_ARM;
		this.pulselib$armPose = PPlayerAnimations.apply(player, model, partialTick, Set.of(playerPart));
		PPlayerMeshDeformers.apply(player, model, partialTick);

		PHumanoidAttachmentLayer.renderFirstPersonArm(
				poseStack,
				packedLight,
				player,
				arm == model.rightArm ? HumanoidArm.RIGHT : HumanoidArm.LEFT,
				arm,
				partialTick);
	}

	@Redirect(method = "renderHand", at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/model/geom/ModelPart;xRot:F",
			opcode = Opcodes.PUTFIELD,
			ordinal = 1))
	private void pulselib$keepAnimatedSleeveRotation(ModelPart sleeve, float xRot)
	{
	}

	@Inject(method = "renderHand", at = @At("RETURN"))
	private void pulselib$restoreArmAnimations(PoseStack poseStack,
	                                            MultiBufferSource bufferSource,
	                                            int packedLight,
	                                            AbstractClientPlayer player,
	                                            ModelPart arm,
	                                            ModelPart sleeve,
	                                            CallbackInfo ci)
	{
		if (this.pulselib$armPose != null)
		{
			this.pulselib$armPose.restore();
			this.pulselib$armPose = null;
		}
	}
}
