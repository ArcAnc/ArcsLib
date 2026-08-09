/**
 * @author ArcAnc
 * Created at: 30.07.2026
 */
package com.arcanc.pulselib.content.mixin;

import com.arcanc.pulselib.content.player.animation.PPlayerAnimations;
import com.arcanc.pulselib.content.player.animation.PPlayerPart;
import com.arcanc.pulselib.content.player.deformer.PPlayerMeshDeformers;
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
<<<<<<< HEAD
=======
	@Unique private @Nullable PPlayerAnimations.PPlayerModelPose pulselib$armPose;

>>>>>>> a625c91 (Added deformers for player and custom models)
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
<<<<<<< HEAD
		ModelPart sleeve = arm == model.rightArm ? model.rightSleeve : model.leftSleeve;
		sleeve.resetPose();
		PPlayerAnimations.apply(player, model, partialTick, Set.of(playerPart));
		PHumanoidAttachmentLayer.renderFirstPersonArm(poseStack, packedLight, player, arm == model.rightArm ? HumanoidArm.RIGHT : HumanoidArm.LEFT, arm, partialTick);
=======
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
>>>>>>> a625c91 (Added deformers for player and custom models)
	}
}
