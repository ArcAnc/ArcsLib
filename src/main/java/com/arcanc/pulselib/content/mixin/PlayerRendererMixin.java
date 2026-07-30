/**
 * @author ArcAnc
 * Created at: 30.07.2026
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
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(AvatarRenderer.class)
public abstract class PlayerRendererMixin
{
	@Unique private boolean pulselib$rootPushed;
	@Unique private boolean pulselib$firstPersonRootPushed;

	@Inject(method = "submit", at = @At("HEAD"))
	private void pulselib$applyPlayerRoot(AvatarRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci)
	{
		if (Minecraft.getInstance().level == null ||
				!(Minecraft.getInstance().level.getEntity(state.id) instanceof AbstractClientPlayer player))
			return;
		poseStack.pushPose();
		this.pulselib$rootPushed = true;
		PPlayerAnimations.applyRoot(player, poseStack, state.partialTick);
	}

	@Inject(method = "submit", at = @At("RETURN"))
	private void pulselib$restorePlayerRoot(AvatarRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci)
	{
		if (this.pulselib$rootPushed)
		{
			poseStack.popPose();
			this.pulselib$rootPushed = false;
		}
	}

	@Inject(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;resetPose()V", shift = At.Shift.AFTER))
	private void pulselib$applyArmAnimation(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, Identifier skinTexture, ModelPart arm, boolean hasSleeve, CallbackInfo ci)
	{
		AbstractClientPlayer player = Minecraft.getInstance().player;
		if (player == null)
			return;
		PlayerModel model = (PlayerModel)(Object)((AvatarRenderer)(Object)this).getModel();
		float partialTick = Minecraft.getInstance().isPaused() ? 0.0f : Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
		PPlayerPart playerPart = arm == model.rightArm ? PPlayerPart.RIGHT_ARM : PPlayerPart.LEFT_ARM;
		PPlayerAnimations.apply(player, model, partialTick, Set.of(playerPart));
		poseStack.pushPose();
		this.pulselib$firstPersonRootPushed = true;
		PPlayerAnimations.applyRoot(player, poseStack, partialTick);
		PHumanoidAttachmentLayer.renderFirstPersonArm(poseStack, packedLight, player, arm == model.rightArm ? HumanoidArm.RIGHT : HumanoidArm.LEFT, arm, partialTick);
	}

	@Inject(method = "renderHand", at = @At("RETURN"))
	private void pulselib$restoreArmAnimations(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, Identifier skinTexture, ModelPart arm, boolean hasSleeve, CallbackInfo ci)
	{
		if (this.pulselib$firstPersonRootPushed)
		{
			poseStack.popPose();
			this.pulselib$firstPersonRootPushed = false;
		}
	}
}
