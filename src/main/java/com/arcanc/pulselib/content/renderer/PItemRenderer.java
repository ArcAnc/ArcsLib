/**
 * @author ArcAnc
 * Created at: 28.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.renderer;


import com.arcanc.pulselib.content.animatable.AnimManagerKey;
import com.arcanc.pulselib.content.animatable.PAnimatable;
import com.arcanc.pulselib.content.animatable.PAnimationController;
import com.arcanc.pulselib.content.animatable.PAnimationManager;
import com.arcanc.pulselib.content.model.animation.BoneFrame;
import com.arcanc.pulselib.content.model.baked.PBakedBone;
import com.arcanc.pulselib.content.model.baked.PBakedModel;
import com.arcanc.pulselib.content.renderer.modelData.PModelData;
import com.arcanc.pulselib.util.PRenderTypes;
import com.arcanc.pulselib.util.PTextureCache;
import com.arcanc.pulselib.util.helpers.PLibRenderHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

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
	public PModelData getModelData(T animatable)
	{
		return this.modelData;
	}
	
	@Override
	public @Nullable PBakedModel getModel(T animatable)
	{
		return getModelData(animatable).getModel();
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
		float partialTick = PLibRenderHelper.mc().isPaused() ? 0f : PLibRenderHelper.mc().getTimer().getGameTimeDeltaPartialTick(false);
		
		poseStack.pushPose();
		poseStack.translate(0.5f, 0, 0.5f);
		poseStack.mulPose(Axis.YP.rotationDegrees(180));
		preSubmit(poseStack, animatable, this :: getRenderType, buffer, packedLight, packedOverlay, partialTick, displayContext);
		trueSubmit(poseStack, animatable, this :: getRenderType, buffer, packedLight, packedOverlay, partialTick, displayContext, stack);
		postSubmit(poseStack, animatable, this :: getRenderType, buffer, packedLight, packedOverlay, partialTick, displayContext);
		poseStack.popPose();
	}
	
	@Override
	public void preSubmit(PoseStack poseStack, T animatable, Function<ResourceLocation, RenderType> renderType, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float partialTick, @Nullable Object... additionalData)
	{
	}
	
	@Override
	public void trueSubmit(PoseStack poseStack, T animatable, Function<ResourceLocation, RenderType> renderType, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float partialTick, @Nullable Object... additionalData)
	{
		PBakedModel model = this.getModelData(animatable).getModel();
		if (model == null)
			return;
		
		ItemStack stack = (ItemStack) additionalData[1];
		
		PAnimationManager<T> manager = animatable.getAnimationManager(AnimManagerKey.of(stack));
		manager.bindModel(model);
		
		Collection<PAnimationController<T>> controllers = manager.getControllers().values();
		ItemDisplayContext context = (ItemDisplayContext)additionalData[0];
		if (context == ItemDisplayContext.GUI)
		{
			model.instantDraw(poseStack, getModelData(animatable),controllers, PRenderTypes.RenderTypeProvider :: trianglesGui, -1, packedLight, packedOverlay, partialTick);
			return;
		}
		model.bones().forEach(bone -> perBoneSubmit(animatable, poseStack, bone, controllers, renderType, -1, packedLight, packedOverlay, partialTick, context));
	}
	
	@Override
	public void postSubmit(PoseStack poseStack, T animatable, Function<ResourceLocation, RenderType> renderType, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float partialTick, @Nullable Object... additionalData)
	{
	
	}
	
	protected void perBoneSubmit(T animatable, PoseStack poseStack, PBakedBone bone, Collection<PAnimationController<T>> controllers, Function<ResourceLocation, RenderType> renderType, int packedColor, int packedLight, int packedOverlay, float partialTick, ItemDisplayContext context)
	{
		PModelData data = this.getModelData(animatable);
		BoneFrame frame = bone.mixBone(data.getModel(), controllers, partialTick);
		poseStack.pushPose();
		if (frame != null)
		{
			poseStack.translate(frame.translation().x(), frame.translation().y(), frame.translation().z());
			poseStack.mulPose(frame.rotation());
			poseStack.scale(frame.scale().x(), frame.scale().y(), frame.scale().z());
		}
		else
		{
			poseStack.translate(bone.basePosition().x(), bone.basePosition().y(), bone.basePosition().z());
			poseStack.mulPose(bone.baseRotation());
		}
		
		this.submitBone(animatable, bone, poseStack, data, controllers, renderType, packedColor, packedLight, packedOverlay, partialTick, context);
		
		if (!bone.children().isEmpty())
			bone.children().forEach(child -> perBoneSubmit(animatable, poseStack, child, controllers, renderType, packedColor, packedLight, packedOverlay, partialTick, context));
		
		poseStack.popPose();
	}
	
	protected void submitBone(T animatable,
	                          PBakedBone bone,
	                          PoseStack poseStack,
	                          PModelData modelData,
	                          Collection<PAnimationController<T>> controllers,
	                          Function<ResourceLocation, RenderType> renderType,
	                          int color,
	                          int packedLight,
	                          int packedOverlay,
	                          float partialTick,
	                          ItemDisplayContext context)
	{
		Matrix4f matrix4fstack = new Matrix4f(poseStack.last().pose());
		
		bone.meshes().forEach(mesh ->
		{
			if (mesh.textureName().isEmpty())
				return;
			
			RenderType type = renderType.apply(PTextureCache.ATLAS_LOCATION);
			if (mesh.isEmissive())
				type = PRenderTypes.RenderTypeProvider.emissiveVariant(type, PTextureCache.ATLAS_LOCATION);
			int meshPackedLight = mesh.isEmissive() ? LightTexture.FULL_BRIGHT : packedLight;
			
			PRenderQueue.submitItem(context, type, mesh.vertexBuffer(), new PRenderQueue.InstanceData(matrix4fstack, color, meshPackedLight, packedOverlay));
		});
	}
}
