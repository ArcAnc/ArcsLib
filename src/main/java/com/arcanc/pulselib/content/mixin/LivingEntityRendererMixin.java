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
<<<<<<< HEAD
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
=======
import com.arcanc.pulselib.content.player.deformer.PPlayerMeshDeformers;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
>>>>>>> a625c91 (Added deformers for player and custom models)
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class LivingEntityRendererMixin
{
	@Inject(method = "setupAnim", at = @At("TAIL"))
	private void pulselib$applyPlayerAnimations(AvatarRenderState state, CallbackInfo ci)
	{
		if (Minecraft.getInstance().level == null ||
				!(Minecraft.getInstance().level.getEntity(state.id) instanceof Player player))
			return;
<<<<<<< HEAD
		PPlayerAnimations.apply(player, (PlayerModel)(Object)this, state.partialTick, PPlayerAnimations.allParts());
=======

		this.pulselib$playerPose = PPlayerAnimations.apply(
				player,
				playerModel,
				partialTick,
				PPlayerAnimations.allParts());
		PPlayerMeshDeformers.apply(player, playerModel, partialTick);
		PPlayerAnimations.applyRoot(player, poseStack, partialTick);
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void pulselib$restorePlayerAnimations(T entity,
	                                               float entityYaw,
	                                               float partialTick,
	                                               PoseStack poseStack,
	                                               MultiBufferSource bufferSource,
	                                               int packedLight,
	                                               CallbackInfo ci)
	{
		if (!(entity instanceof Player) || !(this.model instanceof PlayerModel<?>))
			return;

		if (this.pulselib$playerPose != null)
		{
			this.pulselib$playerPose.restore();
			this.pulselib$playerPose = null;
		}
>>>>>>> a625c91 (Added deformers for player and custom models)
	}
}
