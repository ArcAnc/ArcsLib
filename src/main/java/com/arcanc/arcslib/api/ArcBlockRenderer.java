/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.api;


import com.arcanc.arcslib.content.model.baked.ArcBakedBone;
import com.arcanc.arcslib.content.model.baked.ArcBakedModel;
import com.arcanc.arcslib.util.ArcRenderTypes;
import com.arcanc.arcslib.util.Database;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.OptionalDouble;
import java.util.OptionalInt;

public abstract class ArcBlockRenderer<T extends BlockEntity & ArcAnimatable> implements ArcRenderer<T>, BlockEntityRenderer<@NonNull T, BlockEntityRenderState>
{
	private final ArcModelData model;
	private T animatable;
	private final MappableRingBuffer colorLightOverlay= new MappableRingBuffer(
			() -> Database.rl("color_light_overlay").toLanguageKey(),
			GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE,
			new Std140SizeCalculator().
					putVec4().
					putIVec2().
					putIVec2().
					get());
	public ArcBlockRenderer(@NotNull ArcModelData model)
	{
		this.model = model;
	}
	
	@Override
	public ArcModelData getArcModelData()
	{
		return this.model;
	}
	
	@Override
	public ArcBakedModel getArcModel()
	{
		return this.model.getModel();
	}
	
	@Override
	public T getAnimatable()
	{
		return this.animatable;
	}
	
	@Override
	public BlockEntityRenderState createRenderState()
	{
		return new BlockEntityRenderState();
	}
	
	@Override
	public void extractRenderState(T blockEntity,
	                               BlockEntityRenderState renderState,
	                               float partialTick,
	                               @NonNull Vec3 cameraPosition,
	                               ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress)
	{
		BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
		this.animatable = blockEntity;
	}
	
	@Override
	public void submit(BlockEntityRenderState blockEntityRenderState,
	                   @NonNull PoseStack poseStack,
	                   @NonNull SubmitNodeCollector submitNodeCollector,
	                   @NonNull CameraRenderState cameraRenderState)
	{
		ArcBakedModel model = this.getArcModel();
		poseStack.pushPose();
		poseStack.translate(0.5f, 0, 0.5f);
		model.bones().forEach(bone ->
				perBoneRender(poseStack, blockEntityRenderState, bone, 255, 255, 255, 255, OverlayTexture.NO_OVERLAY));
		poseStack.popPose();
	}
	
	private void perBoneRender(@NonNull PoseStack poseStack,
	                           @NonNull BlockEntityRenderState blockEntityRenderState,
	                           @NonNull ArcBakedBone bone,
	                           int red,
	                           int blue,
	                           int green,
	                           int alpha,
	                           int overlay)
	{
		poseStack.pushPose();
		poseStack.translate(bone.basePosition().x(), bone.basePosition().y(), bone.basePosition().z());
		poseStack.mulPose(bone.baseRotation());
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
			int lightCoords = blockEntityRenderState.lightCoords;
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
				perBoneRender(poseStack, blockEntityRenderState, children, red, green, blue, alpha, overlay));
		poseStack.popPose();
	}
}
