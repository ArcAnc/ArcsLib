/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.renderer;


import com.arcanc.pulselib.content.animatable.PAnimatable;
import com.arcanc.pulselib.content.animatable.instance.PAnimationController;
import com.arcanc.pulselib.content.model.baked.PBakedBone;
import com.arcanc.pulselib.content.model.baked.PBakedModel;
import com.arcanc.pulselib.content.renderer.modelData.PModelData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Function;

public abstract class PBlockRenderer<T extends BlockEntity & PAnimatable<T>>
		implements PRenderer<T>, BlockEntityRenderer<T>
{
	private final PModelData modelData;
	private final Function<ResourceLocation, RenderType> renderType;
	
	public PBlockRenderer(PModelData modelData, Function<ResourceLocation, RenderType> renderType)
	{
		this.modelData = modelData;
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
	public void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay)
	{
		blockEntity.getAnimationManager().getControllers().
				forEach(($, controller) -> controller.tick(blockEntity, this.getModel(), partialTick));
		
		preRender(poseStack, blockEntity, this :: getRenderType, bufferSource, packedLight, packedOverlay, partialTick);
		actuallyRender(poseStack, blockEntity, this :: getRenderType, bufferSource, packedLight, packedOverlay, partialTick);
		postRender(poseStack, blockEntity, this :: getRenderType, bufferSource, packedLight, packedOverlay, partialTick);
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
