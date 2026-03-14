/**
 * @author ArcAnc
 * Created at: 27.02.2026
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
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Function;

public abstract class PEntityRenderer<T extends Entity & PAnimatable<T>> extends EntityRenderer<T>
	implements PRenderer<T>
{
	
	private final PModelData modelData;
	private final Function<ResourceLocation, RenderType> renderType;
	
	public PEntityRenderer(EntityRendererProvider.Context context, PModelData modelData, Function<ResourceLocation, RenderType> renderType)
	{
		super(context);
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
	public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight)
	{
		super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
		
		entity.getAnimationManager().getControllers().
				forEach(($, controller) -> controller.tick(entity, this.getModel(), partialTick));
		
		preRender(poseStack, entity, this :: getRenderType, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, partialTick);
		actuallyRender(poseStack, entity, this :: getRenderType, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, partialTick);
		postRender(poseStack, entity, this :: getRenderType, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, partialTick);
	}
	
	@Override
	public ResourceLocation getTextureLocation(T entity)
	{
		return null;
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
