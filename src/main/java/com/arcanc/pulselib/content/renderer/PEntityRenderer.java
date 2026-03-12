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
import com.arcanc.pulselib.content.model.animation.BoneFrame;
import com.arcanc.pulselib.content.model.baked.PBakedBone;
import com.arcanc.pulselib.content.model.baked.PBakedModel;
import com.arcanc.pulselib.content.renderer.modelData.PModelData;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

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
		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(180));
		this.getModel().bones().forEach(bone ->
				perBoneRender(poseStack, animatable, renderType, bone, controllers, 255, 255, 255, 255, packedLight, packedOverlay, partialTick));
		poseStack.popPose();
	}
	
	@Override
	public void postRender(PoseStack poseStack, T animatable, Function<ResourceLocation, RenderType> renderType, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float partialTick)
	{
	
	}
	
	protected void perBoneRender(PoseStack poseStack,
	                             T animatable,
								 Function<ResourceLocation, RenderType> renderType,
	                             PBakedBone bone,
	                             Collection<PAnimationController<T>> controllers,
	                             int red,
	                             int blue,
	                             int green,
	                             int alpha,
	                             int packedLight,
	                             int packedOverlay,
	                             float partialTick)
	{
		BoneFrame frame = mixBone(bone, controllers);
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
		Matrix4f matrix4fstack = new Matrix4f(RenderSystem.getModelViewMatrix());
		matrix4fstack.mul(poseStack.last().pose());
		
		int blockLight = LightTexture.block(packedLight);
		int skyLight   = LightTexture.sky(packedLight);
		int u = packedOverlay & 0xFFFF;
		int v = (packedOverlay >> 16) & 0xFFFF;
		Vector4f colorVector = new Vector4f(red/255f, green/255f, blue/255f, alpha/255f);
		
		bone.meshes().forEach(mesh ->
		{
			if (mesh.textureName().isEmpty())
				return;
			
			ResourceLocation texture = getTextureByName(mesh.textureName());
			RenderType type = renderType.apply(texture);
			type.setupRenderState();
			
			ShaderInstance shaderInstance = RenderSystem.getShader();
			if (shaderInstance == null)
				return;
			mesh.vertexBuffer().bind();
			shaderInstance.safeGetUniform("Color").set(colorVector);
			shaderInstance.safeGetUniform("Light").set(blockLight, skyLight);
			shaderInstance.safeGetUniform("Overlay").set(u, v);
			shaderInstance.apply();
			mesh.vertexBuffer().drawWithShader(matrix4fstack, RenderSystem.getProjectionMatrix(), shaderInstance);
			VertexBuffer.unbind();
			type.clearRenderState();
		});
		
		bone.children().forEach(children ->
				perBoneRender(poseStack, animatable, renderType, children, controllers, red, green, blue, alpha, packedLight, packedOverlay, partialTick));
		
		poseStack.popPose();
	}
	
	private @Nullable BoneFrame mixBone(
			PBakedBone bone,
			Collection<PAnimationController<T>> controllers)
	{
		Vector3f translation = new Vector3f(bone.basePosition());
		Quaternionf rotation = new Quaternionf(bone.baseRotation());
		Vector3f scale = new Vector3f(1, 1, 1);
		
		boolean hasTransform = false;
		for (PAnimationController<T> controller : controllers)
		{
			BoneFrame frame = controller.calculateBoneTransformations(bone.name(), this.getModel());
			if (frame == null)
				continue;
			translation.add(frame.translation());
			scale.mul(frame.scale());
			rotation.mul(frame.rotation());
			hasTransform = true;
		}
		
		if (!hasTransform)
			return null;
		
		return new BoneFrame(translation, rotation, scale);
	}
}
