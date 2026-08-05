/**
 * @author ArcAnc
 * Created at: 28.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.model.baked;


import com.arcanc.pulselib.content.animatable.PAnimatable;
import com.arcanc.pulselib.content.animatable.PAnimationController;
import com.arcanc.pulselib.content.model.animation.BoneFrame;
import com.arcanc.pulselib.content.model.animation.PAnimationPoseResolver;
import com.arcanc.pulselib.content.model.animation.PPose;
import com.arcanc.pulselib.data.gecko.MolangParser;
import com.arcanc.pulselib.content.renderer.modelData.PModelData;
import com.arcanc.pulselib.util.PLibDatabase;
import com.arcanc.pulselib.util.PRenderTypes;
import com.arcanc.pulselib.util.PTextureCache;
import com.arcanc.pulselib.util.helpers.PLibRenderHelper;
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
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import org.jetbrains.annotations.ApiStatus;
import org.joml.*;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * WARNING! Name should be unique per bone!
 */
public class PBakedBone
{
<<<<<<< HEAD
	private final String name;
	private final Vector3f basePosition;
	private final Quaternionf baseRotation;
	private final List<PBakedBone> children;
	private final @Nullable PBakedBone parent;
	private final List<PBakedMesh> meshes;
	private @Nullable MappableRingBuffer colorLightOverlay;
	private static final int FULL_BRIGHT = 0x00F000F0;
	
	public PBakedBone(String name,
	                  Vector3f basePosition,
	                  Quaternionf baseRotation,
	                  List<PBakedBone> children,
	                  @Nullable PBakedBone parent,
	                  List<PBakedMesh> meshes)
=======
	public void instantDraw(PoseStack poseStack,
	                        PBakedModel model,
	                        PPose pose,
	                        Function<ResourceLocation, RenderType> renderType,
	                        int color,
	                        int packedLight,
	                        int packedOverlay)
	{
		PMeshRenderContext context = new PMeshRenderContext(renderType, color, packedLight, packedOverlay);
		instantDraw(poseStack, model, pose, (bone, mesh, inherited) -> inherited, context);
	}

	public void instantDraw(PoseStack poseStack,
	                        PBakedModel model,
	                        PPose pose,
	                        PMeshRenderResolver resolver,
	                        PMeshRenderContext inherited)
	{
		int boneIndex = model.boneIndex(this);
		poseStack.pushPose();
		poseStack.translate(pose.translation(boneIndex).x(), pose.translation(boneIndex).y(), pose.translation(boneIndex).z());
		poseStack.mulPose(pose.rotation(boneIndex));
		poseStack.scale(pose.scale(boneIndex).x(), pose.scale(boneIndex).y(), pose.scale(boneIndex).z());
		Matrix4f matrix4fstack = new Matrix4f(RenderSystem.getModelViewMatrix());
		matrix4fstack.mul(poseStack.last().pose());

		PMeshRenderContext boneContext = inherited;
		this.meshes().forEach(mesh -> drawMesh(mesh, this, resolver, boneContext, poseStack, matrix4fstack));
		this.children().forEach(child -> child.instantDraw(poseStack, model, pose, resolver, boneContext));
		poseStack.popPose();
	}

	public <T extends PAnimatable<T>> void instantDraw(PoseStack poseStack,
	                                                   PModelData modelData,
	                                                   Collection<PAnimationController<T>> controllers,
	                                                   Function<ResourceLocation, RenderType> renderType,
	                                                   int color,
	                                                   int packedLight,
	                                                   int packedOverlay,
	                                                   float partialTick)
>>>>>>> e194067 (Tons of e)
	{
		this.name = name;
		this.basePosition = basePosition;
		this.baseRotation = baseRotation;
		this.children = children;
		this.parent = parent;
		this.meshes = meshes;
	}
	
	@ApiStatus.Internal
	public void ensureBufferInitialized()
	{
		if (this.colorLightOverlay != null)
			return;
		this.colorLightOverlay = new MappableRingBuffer(
				() -> PLibDatabase.rl("color_overlay").toLanguageKey(),
				GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE,
				new Std140SizeCalculator().
						putVec4().
						putVec2().
						putIVec2().
						get());
	}
	
	public <T extends PAnimatable<T>>void instantDraw(PoseStack poseStack,
	                                                  PModelData modelData,
	                                                  Collection<PAnimationController<T>> controllers,
	                                                  Function<Identifier, RenderType> renderType,
	                                                  int color,
	                                                  int packedOverlay,
	                                                  float partialTick)
	{
		instantDraw(poseStack, modelData, controllers, renderType, color, FULL_BRIGHT, packedOverlay, partialTick);
	}

	public <T extends PAnimatable<T>>void instantDraw(PoseStack poseStack,
	                                                  PModelData modelData,
	                                                  Collection<PAnimationController<T>> controllers,
	                                                  Function<Identifier, RenderType> renderType,
	                                                  int color,
	                                                  int packedLight,
	                                                  int packedOverlay,
	                                                  float partialTick)
	{
		PMeshRenderContext context = new PMeshRenderContext(
				renderType,
				color,
				packedLight,
				packedOverlay);
		instantDraw(poseStack, modelData, controllers, (bone, mesh, inherited) -> inherited, context, partialTick);
	}
	
	public <T extends PAnimatable<T>>void instantDraw(PoseStack poseStack,
	                                                  PModelData modelData,
	                                                  Collection<PAnimationController<T>> controllers,
	                                                  PMeshRenderResolver resolver,
	                                                  PMeshRenderContext inherited,
	                                                  float partialTick)
	{
		BoneFrame frame = mixBone(modelData.getModel(), controllers, partialTick);
		poseStack.pushPose();
		if (frame != null)
		{
			poseStack.translate(frame.translation().x(), frame.translation().y(), frame.translation().z());
			poseStack.mulPose(frame.rotation());
			poseStack.scale(frame.scale().x(), frame.scale().y(), frame.scale().z());
		}
		else
		{
			poseStack.translate(this.basePosition().x(), this.basePosition().y(), this.basePosition().z());
			poseStack.mulPose(this.baseRotation());
		}
		
		Minecraft mc = PLibRenderHelper.mc();
		
		TextureAtlas atlas = PTextureCache.getTextureAtlas();
		Matrix4f matrix4fStack = new Matrix4f(RenderSystem.getModelViewMatrix());
		matrix4fStack.mul(poseStack.last().pose());
		GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().
				writeTransform(matrix4fStack, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector3f(), new Matrix4f());
				
		PMeshRenderContext boneContext = inherited;
		this.meshes().forEach(mesh ->
		{
			if (mesh.textureName().isEmpty())
				return;
			
			PMeshRenderContext meshContext = resolver.resolve(this, mesh, boneContext);
			RenderType baseType = meshContext.renderType().apply(PTextureCache.ATLAS_LOCATION);
			RenderType type = mesh.isEmissive() ?
					PRenderTypes.RenderTypeProvider.instantEmissiveVariant(baseType, PTextureCache.ATLAS_LOCATION) :
					PRenderTypes.RenderTypeProvider.instantVariant(baseType, PTextureCache.ATLAS_LOCATION);

			RenderTarget renderTarget = type.outputTarget().getRenderTarget();

			GpuTextureView colorAttachment = RenderSystem.outputColorTextureOverride != null
					? RenderSystem.outputColorTextureOverride
					: renderTarget.getColorTextureView();

			GpuTextureView depthTexture = renderTarget.useDepth
					? (RenderSystem.outputDepthTextureOverride != null ? RenderSystem.outputDepthTextureOverride : renderTarget.getDepthTextureView())
					: null;
			
			GpuTextureView lightTexture = mc.gameRenderer.levelLightmap();
			OverlayTexture overlayTexture = mc.gameRenderer.overlayTexture();
			
			this.ensureBufferInitialized();
			try (GpuBuffer.MappedView colorLightOverlayMappedView = RenderSystem.getDevice().
					createCommandEncoder().
					mapBuffer(this.colorLightOverlay.currentBuffer(), false, true))
			{
				int u = meshContext.packedOverlay() & 0xFFFF;
				int v = (meshContext.packedOverlay() >> 16) & 0xFFFF;
				
				Std140Builder.intoBuffer(colorLightOverlayMappedView.data()).
						putVec4(ARGB.vector4fFromARGB32(meshContext.color())).
						putVec2(LightCoordsUtil.block(meshContext.packedLight()), LightCoordsUtil.sky(meshContext.packedLight())).
						putIVec2(new Vector2i(u, v));
				
			}
			
			try(RenderPass pass = RenderSystem.getDevice().createCommandEncoder().
					createRenderPass(mesh.uuid() :: toString, colorAttachment, OptionalInt.empty(), depthTexture, OptionalDouble.empty()))
			{
				pass.setPipeline(type.pipeline());
				RenderSystem.bindDefaultUniforms(pass);
				pass.setUniform("ColorOverlay", colorLightOverlay.currentBuffer());
				pass.setUniform("DynamicTransforms", transforms);
				pass.bindTexture("Sampler0", atlas.getTextureView(), atlas.getSampler());
				pass.bindTexture("Sampler1", overlayTexture.getTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
				pass.bindTexture("Sampler2", lightTexture, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
				pass.setVertexBuffer(0, mesh.vbo());
				pass.setIndexBuffer(mesh.indices(), mesh.indexType());
				
				pass.drawIndexed(0, 0, mesh.indicesCount(), 1);
			}
		});
		
		this.children().forEach(children ->
				children.instantDraw(poseStack, modelData, controllers, resolver, boneContext, partialTick));
		
		poseStack.popPose();
	}
	
	public <T extends PAnimatable<T>>@Nullable BoneFrame mixBone(
			PBakedModel model,
			Collection<PAnimationController<T>> controllers,
			float partialTick)
	{
		return mixBone(model, controllers, Map.of(), partialTick);
	}

	public <T extends PAnimatable<T>>@Nullable BoneFrame mixBone(
			PBakedModel model,
			Collection<PAnimationController<T>> controllers,
			Map<PAnimationController<T>, MolangParser.Context> molangContexts,
			float partialTick)
	{
		PAnimationPoseResolver.LocalPose pose = PAnimationPoseResolver.resolveLocal(
				this, model, controllers,
				(controller, tick) -> molangContexts.getOrDefault(controller,
						PAnimationPoseResolver.<T>defaultContexts().context(controller, tick)),
				partialTick);
		return pose.hasTranslation() || pose.hasRotation() || pose.hasScale() ? pose.localTransform() : null;
	}
	
	public String name()
	{
		return this.name;
	}
	
	public Vector3f basePosition()
	{
		return this.basePosition;
	}
	
	public Quaternionf baseRotation()
	{
		return this.baseRotation;
	}
	
	public List<PBakedBone> children()
	{
		return this.children;
	}
	
	@Nullable
	public PBakedBone parent()
	{
		return this.parent;
	}
	
	public List<PBakedMesh> meshes()
	{
		return this.meshes;
	}
	
	public static final class PBakedBoneBuilder
	{
		public final UUID uuid;
		public final String name;
		public final Vector3f basePosition;
		public final Quaternionf baseRotation;
		
		public @Nullable PBakedBoneBuilder parent;
		public final List<PBakedBoneBuilder> children = new ArrayList<>();
		public final List<PBakedMesh> meshes = new ArrayList<>();
		
		public PBakedBoneBuilder(
				UUID uuid,
				String name,
				Vector3f basePosition,
				Quaternionf baseRotation)
		{
			this.uuid = uuid;
			this.name = name;
			this.basePosition = basePosition;
			this.baseRotation = baseRotation;
		}
	}

	private static void drawMesh(PBakedMesh mesh,
	                             PBakedBone bone,
	                             PMeshRenderResolver resolver,
	                             PMeshRenderContext inherited,
	                             PoseStack poseStack,
	                             Matrix4f matrix4fstack)
	{
		if (mesh.textureName().isEmpty())
			return;
		PMeshRenderContext inheritedContext = mesh.isEmissive() ?
				new PMeshRenderContext(inherited.renderType(), inherited.color(), LightTexture.FULL_BRIGHT, inherited.packedOverlay()) : inherited;
		PMeshRenderContext meshContext = resolver.resolve(bone, mesh, inheritedContext);
		RenderType type = meshContext.renderType().apply(PTextureCache.ATLAS_LOCATION);
		if (mesh.isEmissive())
			type = PRenderTypes.RenderTypeProvider.emissiveVariant(type, PTextureCache.ATLAS_LOCATION);
		int u = meshContext.packedOverlay() & 0xFFFF;
		int v = (meshContext.packedOverlay() >> 16) & 0xFFFF;
		int color = meshContext.color();
		Vector4f colorVector = new Vector4f(FastColor.ARGB32.red(color) / 255f, FastColor.ARGB32.green(color) / 255f,
				FastColor.ARGB32.blue(color) / 255f, FastColor.ARGB32.alpha(color) / 255f);
		type.setupRenderState();
		ShaderInstance shader = RenderSystem.getShader();
		if (shader != null)
		{
			mesh.vertexBuffer().bind();
			shader.safeGetUniform("Color").set(colorVector);
			shader.safeGetUniform("Light").set(LightTexture.block(meshContext.packedLight()), LightTexture.sky(meshContext.packedLight()));
			shader.safeGetUniform("Overlay").set(u, v);
			shader.safeGetUniform("NormalMat").set(poseStack.last().normal());
			shader.apply();
			mesh.vertexBuffer().drawWithShader(matrix4fstack, RenderSystem.getProjectionMatrix(), shader);
			VertexBuffer.unbind();
		}
		type.clearRenderState();
	}
}
