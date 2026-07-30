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
import com.arcanc.pulselib.data.MolangParser;
import com.arcanc.pulselib.content.renderer.modelData.PModelData;
import com.arcanc.pulselib.util.PRenderTypes;
import com.arcanc.pulselib.util.PTextureCache;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * WARNING! Name should be unique per bone!
 */
public record PBakedBone(String name,
                         Vector3f basePosition,
                         Quaternionf baseRotation,
                         List<PBakedBone> children,
                         @Nullable PBakedBone parent,
                         List<PBakedMesh> meshes)
{
	public <T extends PAnimatable<T>> void instantDraw(PoseStack poseStack,
	                                                   PModelData modelData,
	                                                   Collection<PAnimationController<T>> controllers,
	                                                   Function<ResourceLocation, RenderType> renderType,
	                                                   int color,
	                                                   int packedLight,
	                                                   int packedOverlay,
	                                                   float partialTick)
	{
		instantDraw(poseStack, modelData, controllers, Map.of(), renderType, color, packedLight, packedOverlay, partialTick);
	}

	public <T extends PAnimatable<T>> void instantDraw(PoseStack poseStack,
	                                                   PModelData modelData,
	                                                   Collection<PAnimationController<T>> controllers,
	                                                   Map<PAnimationController<T>, MolangParser.Context> molangContexts,
	                                                   Function<ResourceLocation, RenderType> renderType,
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
		instantDraw(poseStack, modelData, controllers, molangContexts, (bone, mesh, inherited) -> inherited, context, partialTick);
	}
	
	public <T extends PAnimatable<T>> void instantDraw(PoseStack poseStack,
	                                                   PModelData modelData,
	                                                   Collection<PAnimationController<T>> controllers,
	                                                   PMeshRenderResolver resolver,
	                                                   PMeshRenderContext inherited,
	                                                   float partialTick)
	{
		instantDraw(poseStack, modelData, controllers, Map.of(), resolver, inherited, partialTick);
	}

	public <T extends PAnimatable<T>> void instantDraw(PoseStack poseStack,
	                                                   PModelData modelData,
	                                                   Collection<PAnimationController<T>> controllers,
	                                                   Map<PAnimationController<T>, MolangParser.Context> molangContexts,
	                                                   PMeshRenderResolver resolver,
	                                                   PMeshRenderContext inherited,
	                                                   float partialTick)
	{
		BoneFrame frame = mixBone(modelData.getModel(), controllers, molangContexts, partialTick);
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
		Matrix4f matrix4fstack = new Matrix4f(RenderSystem.getModelViewMatrix());
		matrix4fstack.mul(poseStack.last().pose());
		
		PMeshRenderContext boneContext = inherited;
		this.meshes().forEach(mesh ->
		{
			if (mesh.textureName().isEmpty())
				return;
			
			PMeshRenderContext inheritedContext = mesh.isEmissive() ?
					new PMeshRenderContext(boneContext.renderType(), boneContext.color(), LightTexture.FULL_BRIGHT, boneContext.packedOverlay()) :
					boneContext;
			PMeshRenderContext meshContext = resolver.resolve(this, mesh, inheritedContext);
			
			RenderType type = meshContext.renderType().apply(PTextureCache.ATLAS_LOCATION);
			if (mesh.isEmissive())
				type = PRenderTypes.RenderTypeProvider.emissiveVariant(type, PTextureCache.ATLAS_LOCATION);
			
			int u = meshContext.packedOverlay() & 0xFFFF;
			int v = (meshContext.packedOverlay() >> 16) & 0xFFFF;
			int red = FastColor.ARGB32.red(meshContext.color());
			int green = FastColor.ARGB32.green(meshContext.color());
			int blue = FastColor.ARGB32.blue(meshContext.color());
			int alpha = FastColor.ARGB32.alpha(meshContext.color());
			Vector4f colorVector = new Vector4f(red / 255f, green / 255f, blue / 255f, alpha / 255f);
			
			int meshPackedLight = meshContext.packedLight();
			int blockLight = LightTexture.block(meshPackedLight);
			int skyLight = LightTexture.sky(meshPackedLight);
			type.setupRenderState();
			
			ShaderInstance shaderInstance = RenderSystem.getShader();
			if (shaderInstance == null)
				return;
			mesh.vertexBuffer().bind();
			shaderInstance.safeGetUniform("Color").set(colorVector);
			shaderInstance.safeGetUniform("Light").set(blockLight, skyLight);
			shaderInstance.safeGetUniform("Overlay").set(u, v);
			shaderInstance.safeGetUniform("NormalMat").set(poseStack.last().normal());
			shaderInstance.apply();
			mesh.vertexBuffer().drawWithShader(matrix4fstack, RenderSystem.getProjectionMatrix(), shaderInstance);
			VertexBuffer.unbind();
			type.clearRenderState();
		});
		
		this.children().forEach(children ->
				children.instantDraw(poseStack, modelData, controllers, molangContexts, resolver, boneContext, partialTick));
		
		poseStack.popPose();
	}
	
	public <T extends PAnimatable<T>>@Nullable BoneFrame mixBone(
			PBakedModel model,
			Collection<PAnimationController<T>> controllers, float partialTick)
	{
		return mixBone(model, controllers, Map.of(), partialTick);
	}

	public <T extends PAnimatable<T>>@Nullable BoneFrame mixBone(
			PBakedModel model,
			Collection<PAnimationController<T>> controllers,
			Map<PAnimationController<T>, MolangParser.Context> molangContexts,
			float partialTick)
	{
		Vector3f translation = new Vector3f(this.basePosition());
		Quaternionf rotation = new Quaternionf(this.baseRotation());
		Vector3f scale = new Vector3f(1, 1, 1);
		
		boolean hasTransform = false;
		for (PAnimationController<T> controller : controllers)
		{
			MolangParser.Context molangContext = molangContexts.get(controller);
			if (molangContext == null)
				molangContext = new MolangParser.Context().
						query("anim_time", controller.getInterpolatedTime(partialTick) / 20f).
						randomSeed(0L);
			BoneFrame frame = controller.calculateBoneTransformations(
					this.name(),
					model,
					partialTick,
					molangContext,
					new BoneFrame(new Vector3f(translation), new Quaternionf(rotation), new Vector3f(scale)));
			if (frame == null)
				continue;
			translation.add(frame.translation());
			scale.mul(frame.scale());
			rotation.premul(frame.rotation());
			hasTransform = true;
		}
		
		if (!hasTransform)
			return null;
		
		return new BoneFrame(translation, rotation, scale);
	}
	
	public static final class PBakedBoneBuilder
	{
		public final UUID uuid;
		public final String name;
		public final Vector3f basePosition;
		public final Quaternionf baseRotation;
		
		public PBakedBoneBuilder parent;
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
}
