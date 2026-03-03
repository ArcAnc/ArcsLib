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
import com.arcanc.arclib.content.renderer.base.ArcEntityRenderState;
import com.arcanc.arclib.content.renderer.base.ArcRenderState;
import com.arcanc.arclib.content.renderer.modelData.ArcModelData;
import com.arcanc.arclib.util.ArcRenderTypes;
import com.arcanc.arclib.util.Database;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.Entity;
import org.joml.*;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public abstract class ArcEntityRenderer<T extends Entity & ArcAnimatable<T>, RS extends EntityRenderState & ArcEntityRenderState<T>> extends EntityRenderer<T, RS>
	implements ArcRenderer<T, RS>
{
	
	private final ArcModelData modelData;
	private final MappableRingBuffer colorLightOverlay= new MappableRingBuffer(
			() -> Database.rl("color_light_overlay").toLanguageKey(),
			GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE,
			new Std140SizeCalculator().
					putVec4().
					putIVec2().
					putIVec2().
					get());
	
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
	public void extractRenderState(T entity, RS renderState, float partialTicks)
	{
		super.extractRenderState(entity, renderState, partialTicks);
		renderState.extractEntityData(entity, this);
	}
	
	@Override
	public void submit(RS renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState)
	{
		super.submit(renderState, poseStack, submitNodeCollector, cameraRenderState);
		renderState.getAnimatable().getAnimationManager().getControllers().
				forEach((_, controller) -> controller.tick(renderState.getAnimatable(), renderState));
		
		preRender(poseStack, renderState, cameraRenderState, submitNodeCollector);
		actuallyRender(poseStack, renderState, cameraRenderState, submitNodeCollector);
		postRender(poseStack, renderState, cameraRenderState, submitNodeCollector);
	}
	
	@Override
	public void preRender(PoseStack poseStack, RS renderState, CameraRenderState cameraRenderState, SubmitNodeCollector submitNodeCollector)
	{
	
	}
	
	@Override
	public void actuallyRender(PoseStack poseStack, RS renderState, CameraRenderState cameraRenderState, SubmitNodeCollector submitNodeCollector)
	{
		Collection<ArcAnimationController<T>> controllers = renderState.getAnimatable().getAnimationManager().getControllers().values();
		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(180));
		renderState.getBakedModel().bones().forEach(bone ->
				perBoneRender(poseStack, renderState, bone, controllers, 255, 255, 255, 255, OverlayTexture.NO_OVERLAY));
		poseStack.popPose();
	}
	
	@Override
	public void postRender(PoseStack poseStack, RS renderState, CameraRenderState cameraRenderState, SubmitNodeCollector submitNodeCollector)
	{
	
	}
	
	protected void perBoneRender(PoseStack poseStack,
	                           RS renderState,
	                           ArcBakedBone bone,
	                           Collection<ArcAnimationController<T>> controllers,
	                           int red,
	                           int blue,
	                           int green,
	                           int alpha,
	                           int overlay)
	{
		BoneFrame frame = mixBone(bone, controllers, renderState);
		
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
		
		RenderTarget framebuffer = Minecraft.getInstance().getMainRenderTarget();
		GpuTextureView colorAttachment = framebuffer.getColorTextureView();
		GpuTextureView depthTexture = framebuffer.getDepthTextureView();
		Matrix4f matrix4fstack = new Matrix4f(RenderSystem.getModelViewMatrix());
		matrix4fstack.mul(poseStack.last().pose());
		GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().
				writeTransform(matrix4fstack, new Vector4f(red/255f, green/255f, blue/255f, alpha/255f), new Vector3f(), new Matrix4f());
		try (GpuBuffer.MappedView colorLightOverlayMappedView = RenderSystem.getDevice().
				createCommandEncoder().
				mapBuffer(this.colorLightOverlay.currentBuffer(), false, true))
		{
			int lightCoords = renderState.lightCoords;
			int blockLight = LightCoordsUtil.block(lightCoords);
			int skyLight   = LightCoordsUtil.sky(lightCoords);
			int u = overlay & 0xFFFF;
			int v = (overlay >> 16) & 0xFFFF;
			Std140Builder.intoBuffer(colorLightOverlayMappedView.data()).
					putVec4(ARGB.vector4fFromARGB32(ARGB.color(alpha, red, green, blue))).
					putIVec2(new Vector2i(blockLight, skyLight)).
					putIVec2(new Vector2i(u, v));
		}
		bone.meshes().forEach(mesh ->
		{
			if (mesh.textureName().isEmpty())
				return;
			
			Minecraft minecraft = Minecraft.getInstance();
			TextureManager tm = minecraft.getTextureManager();
			AbstractTexture texture = tm.getTexture(getTextureByName(mesh.textureName()));
			GpuTextureView lightTexture = minecraft.gameRenderer.levelLightmap();
			OverlayTexture overlayTexture = minecraft.gameRenderer.overlayTexture();
			try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(mesh.uuid() :: toString, colorAttachment, OptionalInt.empty(), depthTexture, OptionalDouble.empty()))
			{
				pass.setPipeline(ArcRenderTypes.RenderPipelinesProvider.TRIANGLES_SOLID);
				RenderSystem.bindDefaultUniforms(pass);
				pass.setVertexBuffer(0, mesh.vbo());
				pass.setIndexBuffer(mesh.indices(), mesh.indexType());
				pass.setUniform("ColorLightOverlay", colorLightOverlay.currentBuffer());
				pass.setUniform("DynamicTransforms", transforms);
				pass.bindTexture("Sampler0", texture.getTextureView(), texture.getSampler());
				pass.bindTexture("Sampler1", overlayTexture.getTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
				pass.bindTexture("Sampler2", lightTexture, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
				
				pass.drawIndexed(0, 0, mesh.indicesCount(), 1);
			}
		});
		
		bone.children().forEach(children ->
				perBoneRender(poseStack, renderState, children, controllers, red, green, blue, alpha, overlay));
		
		poseStack.popPose();
	}
	
	private @Nullable BoneFrame mixBone(
			ArcBakedBone bone,
			Collection<ArcAnimationController<T>> controllers,
			ArcRenderState<T> state)
	{
		Vector3f translation = new Vector3f(bone.basePosition());
		Quaternionf rotation = new Quaternionf(bone.baseRotation());
		Vector3f scale = new Vector3f(1, 1, 1);
		
		boolean hasTransform = false;
		for (ArcAnimationController<T> controller : controllers)
		{
			BoneFrame frame = controller.calculateBoneTransformations(bone.name(), state);
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
