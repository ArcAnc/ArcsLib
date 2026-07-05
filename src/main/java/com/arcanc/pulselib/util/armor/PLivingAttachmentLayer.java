/**
 * @author ArcAnc
 * Created at: 05.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.util.armor;


import com.arcanc.pulselib.content.animatable.PAnimationController;
import com.arcanc.pulselib.content.model.baked.PBakedBone;
import com.arcanc.pulselib.content.model.baked.PBakedModel;
import com.arcanc.pulselib.util.PLibDatabase;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

public class PLivingAttachmentLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M>
{
	public static final ContextKey<List<RenderEntry>> RENDER_DATA = new ContextKey<>(PLibDatabase.rl("living_attachments"));

	protected static final List<EquipmentSlot> EQUIPMENT_SLOTS = List.of(
			EquipmentSlot.HEAD,
			EquipmentSlot.CHEST,
			EquipmentSlot.LEGS,
			EquipmentSlot.FEET,
			EquipmentSlot.BODY,
			EquipmentSlot.MAINHAND,
			EquipmentSlot.OFFHAND);

	public PLivingAttachmentLayer(RenderLayerParent<S, M> parent)
	{
		super(parent);
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, S state, float yRot, float xRot)
	{
		List<RenderEntry> entries = state.getRenderData(RENDER_DATA);
		if (entries == null)
			return;

		for (RenderEntry entry : entries)
			renderDefinition(poseStack, light, state.partialTick, entry,
					attachment -> PulseAttachmentAnchorResolvers.resolve(state, this.getParentModel(), attachment.anchor()));
	}

	public static List<RenderEntry> extractRenderEntries(LivingEntity entity, float partialTick)
	{
		List<RenderEntry> entries = new ArrayList<>();
		for (EquipmentSlot slot : EQUIPMENT_SLOTS)
		{
			ItemStack stack = entity.getItemBySlot(slot);
			for (PulseLivingDefinition definition : PulseLivingAttachments.get(stack, slot, entity))
				addRenderEntry(entries, entity, stack, definition, partialTick);
		}

		for (PulseLivingDefinition definition : PulseLivingAttachments.getGlobal(entity))
			addRenderEntry(entries, entity, ItemStack.EMPTY, definition, partialTick);

		return entries;
	}

	private static void addRenderEntry(List<RenderEntry> entries,
	                                   LivingEntity entity,
	                                   ItemStack stack,
	                                   PulseLivingDefinition definition,
	                                   float partialTick)
	{
		PBakedModel model = definition.modelData().getModel();
		if (model == null)
			return;

		Collection<PAnimationController<?>> controllers = definition.animationControllers(entity, stack, model, partialTick);
		entries.add(new RenderEntry(definition, stack.copy(), controllers));
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

			for (RenderEntry entry : extractRenderEntries(entity, partialTick))
				renderDefinition(poseStack, light, partialTick, entry,
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
			RenderEntry entry,
			AttachmentPartResolver partResolver)
	{
		PulseLivingDefinition definition = entry.definition();
		PBakedModel model = definition.modelData().getModel();
		if (model == null)
			return;

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

			drawBone(poseStack, definition, bone, attachment, entry.controllers(), light, partialTick);

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

	public record RenderEntry(
			PulseLivingDefinition definition,
			ItemStack stack,
			Collection<PAnimationController<?>> controllers)
	{
	}
}
