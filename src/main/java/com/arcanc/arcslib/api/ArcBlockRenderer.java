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
import com.arcanc.arcslib.content.model.baked.ArcBakedMesh;
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
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CloudRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.OptionalDouble;
import java.util.OptionalInt;

public abstract class ArcBlockRenderer<T extends BlockEntity & ArcAnimatable> implements ArcRenderer<T>, BlockEntityRenderer<T, BlockEntityRenderState>
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
				perBoneRender(poseStack, bone, 255, 255, 255, 255, blockEntityRenderState.lightCoords));
		poseStack.popPose();
	}
	
	private void perBoneRender(@NonNull PoseStack poseStack,
	                           @NonNull ArcBakedBone bone,
	                           int red,
	                           int blue,
	                           int green,
	                           int alpha,
	                           int light)
	{
		poseStack.pushPose();
		poseStack.translate(bone.basePosition().x(), bone.basePosition().y(), bone.basePosition().z());
		poseStack.mulPose(bone.baseRotation());
		RenderTarget framebuffer = Minecraft.getInstance().getMainRenderTarget();
		GpuTextureView colorAttachment = framebuffer.getColorTextureView();
		GpuTextureView depthTexture = framebuffer.getDepthTextureView();
		GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().
				writeTransform(RenderSystem.getModelViewMatrix(), new Vector4f(red/255f, green/255f, blue/255f, alpha/255f), new Vector3f(), new Matrix4f());
		
		try (GpuBuffer.MappedView colorLightOverlayMappedView = RenderSystem.getDevice().
						createCommandEncoder().
						mapBuffer(this.colorLightOverlay.currentBuffer(), false, true))
		{
			Std140Builder.intoBuffer(colorLightOverlayMappedView.data()).
							putVec4(ARGB.vector4fFromARGB32(ARGB.color(alpha, red, green, blue))).
							putIVec2(new Vector2i(LightTexture.FULL_BRIGHT)).
							putIVec2(new Vector2i(0, 10));
		}
		bone.meshes().forEach(mesh ->
		{
			
			if (mesh.textureId() == - 1)
				return;
			BufferBuilder meshBuffer = rebuildMesh(mesh);
			GpuBuffer meshVbo;
			try(MeshData meshData = meshBuffer.buildOrThrow())
			{
				meshVbo = RenderSystem.getDevice().createBuffer(
						mesh.uuid() :: toString,
						GpuBuffer.USAGE_VERTEX,
						meshData.vertexBuffer());
			}
			RenderSystem.AutoStorageIndexBuffer meshIndices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.TRIANGLES);
			GpuBuffer indices = meshIndices.getBuffer(64);
			
			Minecraft minecraft = Minecraft.getInstance();
			TextureManager tm = minecraft.getTextureManager();
			AbstractTexture texture = tm.getTexture(getTextureById(mesh.textureId()));
			LightTexture lightTexture = minecraft.gameRenderer.lightTexture();
			OverlayTexture overlay = minecraft.gameRenderer.overlayTexture();
			try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(mesh.uuid() :: toString, colorAttachment, OptionalInt.empty(), depthTexture, OptionalDouble.empty()))
			{
				RenderSystem.bindDefaultUniforms(pass);
				pass.setPipeline(ArcRenderTypes.RenderPipelinesProvider.TRIANGLES_SOLID);
				pass.setVertexBuffer(0, meshVbo);
				pass.setIndexBuffer(indices, meshIndices.type());
				pass.setUniform("ColorLightOverlay", colorLightOverlay.currentBuffer());
				pass.setUniform("DynamicTransforms", transforms);
				pass.bindTexture("Sampler0", texture.getTextureView(), texture.getSampler());
				pass.bindTexture("Sampler1", overlay.getTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
				pass.bindTexture("Sampler2", lightTexture.getTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
				
				pass.drawIndexed(0, 0, mesh.vertexesAmount(), 1);
			}
		});
		
		
		bone.children().forEach(children ->
				perBoneRender(poseStack, children, red, green, blue, alpha, light));
		poseStack.popPose();
	}
	
	private @NonNull BufferBuilder rebuildMesh(@NonNull ArcBakedMesh mesh)
	{
		ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.
				exactlySized(mesh.vertexesAmount() * ArcRenderTypes.VertexFormatProvider.POSITION_TEX_NORMAL.getVertexSize());
		BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.TRIANGLES, ArcRenderTypes.VertexFormatProvider.POSITION_TEX_NORMAL);
		
		for (int q = 0; q < mesh.vertexesAmount(); q++)
		{
			bufferBuilder.addVertex(
					mesh.positions().get(q * 3),
					mesh.positions().get(q * 3 + 1),
					mesh.positions().get(q * 3 + 2)).
					setUv(
					mesh.uvs().get(q * 2),
					mesh.uvs().get(q * 2 + 1)).
					setNormal(
					mesh.normals().get(q * 3),
					mesh.normals().get(q * 3 + 1),
					mesh.normals().get(q * 3 + 2));
		}
		return bufferBuilder;
	}
}
