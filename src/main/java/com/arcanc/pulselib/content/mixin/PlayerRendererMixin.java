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
import com.arcanc.pulselib.util.attachments.humanoid.PHumanoidAttachmentLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(AvatarRenderer.class)
public abstract class PlayerRendererMixin
{
	@Inject(method = "renderHand", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModelPart(Lnet/minecraft/client/model/geom/ModelPart;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IILnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V",
			shift = At.Shift.BEFORE))
	private void pulselib$applyArmAnimation(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, Identifier skinTexture, ModelPart arm, boolean hasSleeve, CallbackInfo ci)
	{
		AbstractClientPlayer player = Minecraft.getInstance().player;
		if (player == null)
			return;
		PlayerModel model = (PlayerModel)(Object)((AvatarRenderer)(Object)this).getModel();
		float partialTick = Minecraft.getInstance().isPaused() ? 0.0f : Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
		PPlayerPart playerPart = arm == model.rightArm ? PPlayerPart.RIGHT_ARM : PPlayerPart.LEFT_ARM;
		ModelPart sleeve = arm == model.rightArm ? model.rightSleeve : model.leftSleeve;
		sleeve.resetPose();
		PPlayerAnimations.apply(player, model, partialTick, Set.of(playerPart));
		PHumanoidAttachmentLayer.renderFirstPersonArm(poseStack, packedLight, player, arm == model.rightArm ? HumanoidArm.RIGHT : HumanoidArm.LEFT, arm, partialTick);
	}
}
