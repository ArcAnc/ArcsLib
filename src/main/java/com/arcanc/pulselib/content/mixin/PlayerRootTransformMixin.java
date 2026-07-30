/**
 * @author ArcAnc
 * Created at: 30.07.2026
 */
package com.arcanc.pulselib.content.mixin;

import com.arcanc.pulselib.content.player.animation.PPlayerAnimations;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class PlayerRootTransformMixin
{
	@Unique private boolean pulselib$rootPushed;

	@Inject(
			method = "submit",
			at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V", ordinal = 1, shift = At.Shift.AFTER))
	private void pulselib$applyPlayerRoot(LivingEntityRenderState state,
	                                     PoseStack poseStack,
	                                     SubmitNodeCollector submitNodeCollector,
	                                     CameraRenderState camera,
	                                     CallbackInfo ci)
	{
		if (!(state instanceof AvatarRenderState avatarState) || Minecraft.getInstance().level == null ||
				!(Minecraft.getInstance().level.getEntity(avatarState.id) instanceof AbstractClientPlayer player))
			return;

		poseStack.pushPose();
		this.pulselib$rootPushed = true;
		PPlayerAnimations.applyRoot(player, poseStack, avatarState.partialTick);
	}

	@Inject(
			method = "submit",
			at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", ordinal = 0, shift = At.Shift.BEFORE))
	private void pulselib$restorePlayerRoot(LivingEntityRenderState state,
	                                       PoseStack poseStack,
	                                       SubmitNodeCollector submitNodeCollector,
	                                       CameraRenderState camera,
	                                       CallbackInfo ci)
	{
		if (this.pulselib$rootPushed)
		{
			poseStack.popPose();
			this.pulselib$rootPushed = false;
		}
	}
}
