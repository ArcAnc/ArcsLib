/**
 * @author ArcAnc
 * Created at: 28.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.renderer;


import com.arcanc.pulselib.content.animatable.PAnimatable;
import com.arcanc.pulselib.content.animatable.instance.PAnimationController;
import com.arcanc.pulselib.content.model.animation.BoneFrame;
import com.arcanc.pulselib.content.model.baked.PBakedBone;
import com.arcanc.pulselib.content.model.baked.PBakedModel;
import com.arcanc.pulselib.content.renderer.modelData.PModelData;
import com.arcanc.pulselib.util.helpers.RenderHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Collection;
import java.util.function.Function;

public abstract class PItemRenderer<T extends Item & PAnimatable<T>> extends BlockEntityWithoutLevelRenderer implements PRenderer<T>
{
	private final PModelData modelData;
	private final Function<ResourceLocation, RenderType> renderType;
	public PItemRenderer(PModelData data, Function<ResourceLocation, RenderType> renderType, BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelSet entityModelSet)
	{
		super(blockEntityRenderDispatcher, entityModelSet);
		this.modelData = data;
		this.renderType = renderType;
	}
	
	@Override
	public PModelData getModelData()
	{
		return this.modelData;
	}
	
	@Override
	public @Nullable PBakedModel getModel()
	{
		return this.modelData.getModel();
	}
	
	@Override
	public RenderType getRenderType(ResourceLocation texture)
	{
		return this.renderType.apply(texture);
	}
	
	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay)
	{
		@SuppressWarnings("unchecked")
		T animatable = stack.getItem() instanceof PAnimatable ? (T) stack.getItem() : null;
		if (animatable == null)
			return;
		float partialTick = RenderHelper.mc().getTimer().getGameTimeDeltaPartialTick(false);
		
		preRender(poseStack, animatable, this :: getRenderType, buffer, packedLight, packedOverlay, partialTick);
		actuallyRender(poseStack, animatable, this :: getRenderType, buffer, packedLight, packedOverlay, partialTick);
		postRender(poseStack, animatable, this :: getRenderType, buffer, packedLight, packedOverlay, partialTick);
	}
	
	@Override
	public void preRender(PoseStack poseStack, T animatable, Function<ResourceLocation, RenderType> renderType, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float partialTick)
	{
	
	}
	
	@Override
	public void actuallyRender(PoseStack poseStack, T animatable, Function<ResourceLocation, RenderType> renderType, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float partialTick)
	{
		Collection<PAnimationController<T>> controllers = animatable.getAnimationManager().getControllers().values();
		PBakedModel model = this.getModel();
		if (model == null)
			return;
		poseStack.pushPose();
		poseStack.translate(0.5f, 0, 0.5f);
		poseStack.mulPose(Axis.YP.rotationDegrees(180));
		model.bones().forEach(bone -> perBoneRenderer(poseStack, bone, controllers, renderType, -1, packedLight, packedOverlay, partialTick));
		poseStack.popPose();
	}
	
	@Override
	public void postRender(PoseStack poseStack, T animatable, Function<ResourceLocation, RenderType> renderType, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float partialTick)
	{
	
	}
	
	protected void perBoneRenderer(PoseStack poseStack, PBakedBone bone, Collection<PAnimationController<T>> controllers, Function<ResourceLocation, RenderType> renderType, int packedColor, int packedLight, int packedOverlay, float partialTick)
	{
		bone.render(poseStack, this.getModelData(), controllers, renderType, packedColor, packedLight, packedOverlay, partialTick);
	}
}
