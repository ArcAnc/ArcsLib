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
import com.arcanc.pulselib.content.animatable.instance.PAnimationController;
import com.arcanc.pulselib.content.model.animation.BoneFrame;
import com.arcanc.pulselib.content.renderer.modelData.PModelData;
import com.arcanc.pulselib.util.Database;
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
import java.util.UUID;
import java.util.function.Function;

/**
 * WARNING! Name should be unique per bone!
 */
public record PBakedBone(String name,
                         Vector3f basePosition,
                         Quaternionf baseRotation,
                         List<PBakedBone> children,
                         PBakedBone parent,
                         List<PBakedMesh> meshes)
{
	public <T extends PAnimatable<T>>void instantDraw(PoseStack poseStack,
	                                             PModelData modelData,
	                                             Collection<PAnimationController<T>> controllers,
	                                             Function<ResourceLocation, RenderType> renderType,
	                                             int color,
	                                             int packedLight,
	                                             int packedOverlay,
	                                             float partialTick)
	{
		BoneFrame frame = mixBone(modelData.getModel(), controllers);
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
		
		int blockLight = LightTexture.block(packedLight);
		int skyLight   = LightTexture.sky(packedLight);
		int u = packedOverlay & 0xFFFF;
		int v = (packedOverlay >> 16) & 0xFFFF;
		int red = FastColor.ARGB32.red(color);
		int green = FastColor.ARGB32.green(color);
		int blue = FastColor.ARGB32.blue(color);
		int alpha = FastColor.ARGB32.alpha(color);
		
		Vector4f colorVector = new Vector4f(red/255f, green/255f, blue/255f, alpha/255f);
		
		this.meshes().forEach(mesh ->
		{
			if (mesh.textureName().isEmpty())
				return;
			
			ResourceLocation texture = modelData.getTextureByName(mesh.textureName());
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
		
		this.children().forEach(children ->
				children.instantDraw(poseStack, modelData, controllers, renderType, color, packedLight, packedOverlay, partialTick));
		
		poseStack.popPose();
	}
	
	public <T extends PAnimatable<T>>@Nullable BoneFrame mixBone(
			PBakedModel model,
			Collection<PAnimationController<T>> controllers)
	{
		Vector3f translation = new Vector3f(this.basePosition());
		Quaternionf rotation = new Quaternionf(this.baseRotation());
		Vector3f scale = new Vector3f(1, 1, 1);
		
		boolean hasTransform = false;
		for (PAnimationController<?> controller : controllers)
		{
			BoneFrame frame = controller.calculateBoneTransformations(this.name(), model);
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
