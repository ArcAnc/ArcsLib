/**
 * @author ArcAnc
 * Created at: 27.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arclib.content.renderer;


import com.arcanc.arclib.content.animatable.ArcAnimatable;
import com.arcanc.arclib.content.animatable.instance.ArcAnimationController;
import com.arcanc.arclib.content.model.animation.BoneFrame;
import com.arcanc.arclib.content.model.baked.ArcBakedBone;
import com.arcanc.arclib.content.model.baked.ArcBakedModel;
import com.arcanc.arclib.content.renderer.modelData.ArcModelData;
import com.arcanc.arclib.util.ArcRenderTypes;
import com.arcanc.arclib.util.helpers.RenderHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
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

public abstract class ArcEntityRenderer<T extends Entity & ArcAnimatable<T>> extends EntityRenderer<T>
	implements ArcRenderer<T>
{
	
	private final ArcModelData modelData;
	
	public ArcEntityRenderer(EntityRendererProvider.Context context, ArcModelData modelData)
	{
		super(context);
		this.modelData = modelData;
	}
	
	@Override
	public ArcModelData getArcModelData()
	{
		return this.modelData;
	}
	
	@Override
	public ArcBakedModel getArcModel()
	{
		return this.modelData.getModel();
	}
	
	@Override
	public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight)
	{
		super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
		
		entity.getAnimationManager().getControllers().
				forEach(($, controller) -> controller.tick(entity, this.getArcModel(), partialTick));
		
		preRender(poseStack, entity, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, partialTick);
		actuallyRender(poseStack, entity, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, partialTick);
		postRender(poseStack, entity, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, partialTick);
	}
	
	@Override
	public ResourceLocation getTextureLocation(T entity)
	{
		return null;
	}
	
	@Override
	public void preRender(PoseStack poseStack, T animatable, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float partialTick)
	{
	
	}
	
	@Override
	public void actuallyRender(PoseStack poseStack, T animatable, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float partialTick)
	{
		Collection<ArcAnimationController<T>> controllers = animatable.getAnimationManager().getControllers().values();
		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(180));
		this.getArcModel().bones().forEach(bone ->
				perBoneRender(poseStack, animatable, bone, controllers, 255, 255, 255, 255, packedLight, packedOverlay, partialTick));
		poseStack.popPose();
	}
	
	@Override
	public void postRender(PoseStack poseStack, T animatable, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float partialTick)
	{
	
	}
	
	protected void perBoneRender(PoseStack poseStack,
	                             T animatable,
	                             ArcBakedBone bone,
	                             Collection<ArcAnimationController<T>> controllers,
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
		Minecraft mc = RenderHelper.mc();
		
		bone.meshes().forEach(mesh ->
		{
			if (mesh.textureName().isEmpty())
				return;
			
			RenderSystem.enableDepthTest();
			RenderSystem.depthMask(true);
			ShaderInstance shaderInstance = ArcRenderTypes.ShadersProvider.TRIANGLES_SHADER;
			RenderSystem.setShader(() -> shaderInstance);
			mesh.vertexBuffer().bind();
			RenderSystem.setShaderTexture(0, getTextureByName(mesh.textureName()));
			mc.gameRenderer.overlayTexture().setupOverlayColor();
			mc.gameRenderer.lightTexture().turnOnLightLayer();
			shaderInstance.getUniform("Color").set(colorVector);
			shaderInstance.getUniform("Light").set(blockLight, skyLight);
			shaderInstance.getUniform("Overlay").set(u, v);
			
			shaderInstance.apply();
			mesh.vertexBuffer().drawWithShader(matrix4fstack, RenderSystem.getProjectionMatrix(), shaderInstance);
			VertexBuffer.unbind();
			RenderSystem.depthMask(false);
			RenderSystem.disableDepthTest();
		});
		
		bone.children().forEach(children ->
				perBoneRender(poseStack, animatable, children, controllers, red, green, blue, alpha, packedLight, packedOverlay, partialTick));
		
		poseStack.popPose();
	}
	
	private @Nullable BoneFrame mixBone(
			ArcBakedBone bone,
			Collection<ArcAnimationController<T>> controllers)
	{
		Vector3f translation = new Vector3f(bone.basePosition());
		Quaternionf rotation = new Quaternionf(bone.baseRotation());
		Vector3f scale = new Vector3f(1, 1, 1);
		
		boolean hasTransform = false;
		for (ArcAnimationController<T> controller : controllers)
		{
			BoneFrame frame = controller.calculateBoneTransformations(bone.name(), this.getArcModel());
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
