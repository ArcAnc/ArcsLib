/**
 * @author ArcAnc
 * Created at: 20.05.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.renderer;


import com.arcanc.pulselib.content.animatable.PAnimatable;
import com.arcanc.pulselib.content.animatable.PAnimationController;
import com.arcanc.pulselib.content.model.baked.PBakedBone;
import com.arcanc.pulselib.content.model.baked.PBakedMesh;
import com.arcanc.pulselib.content.model.baked.PBakedModel;
import com.arcanc.pulselib.content.model.baked.PMeshRenderContext;
import com.arcanc.pulselib.content.renderer.base.PEntityRenderState;
import com.arcanc.pulselib.content.renderer.modelData.PModelData;
import com.arcanc.pulselib.data.gecko.MolangParser;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class PEntityRenderLayer<T extends Entity & PAnimatable<T>, RS extends EntityRenderState & PEntityRenderState<T>>
{
	private final PModelData modelData;
	private final Function<Identifier, RenderType> renderType;
	private final Map<String, String> boneBindings = new HashMap<>();
	private final Vector3f offset = new Vector3f();
	private final Quaternionf rotation = new Quaternionf();
	private final Vector3f scale = new Vector3f(1, 1, 1);
	
	public PEntityRenderLayer(PModelData modelData, Function<Identifier, RenderType> renderType)
	{
		this.modelData = modelData;
		this.renderType = renderType;
	}
	
	public PModelData getModelData(RS renderState)
	{
		return this.modelData;
	}
	
	public @Nullable PBakedModel getModel(RS renderState)
	{
		return getModelData(renderState).getModel();
	}
	
	public RenderType getRenderType(Identifier texture)
	{
		return this.renderType.apply(texture);
	}

	public PEntityRenderLayer<T, RS> bindBone(String layerBoneName, String entityBoneName)
	{
		this.boneBindings.put(layerBoneName, entityBoneName);
		return this;
	}

	public PEntityRenderLayer<T, RS> bindMatchingBone(String boneName)
	{
		return bindBone(boneName, boneName);
	}

	public PEntityRenderLayer<T, RS> bindMatchingBones(String... boneNames)
	{
		for (String boneName : boneNames)
			bindMatchingBone(boneName);
		return this;
	}

	public @Nullable String getBoundEntityBone(String layerBoneName)
	{
		return this.boneBindings.get(layerBoneName);
	}
	
	public boolean shouldRender(RS renderState)
	{
		return true;
	}
	
	public int getColor(RS renderState, PBakedBone bone, PBakedMesh mesh, int packedColor)
	{
		return packedColor;
	}
	
	public int getPackedLight(RS renderState, int packedLight)
	{
		return packedLight;
	}
	
	public int getPackedOverlay(RS renderState, int packedOverlay)
	{
		return packedOverlay;
	}
	
	public PMeshRenderContext resolveMeshRender(RS renderState,
	                                            PBakedBone bone,
	                                            PBakedMesh mesh,
	                                            PMeshRenderContext inherited)
	{
		return inherited.
				withColor(getColor(renderState, bone, mesh, inherited.color())).
				withPackedLight(getPackedLight(renderState, inherited.packedLight())).
				withPackedOverlay(getPackedOverlay(renderState, inherited.packedOverlay()));
	}
	
	public void submit(PEntityRenderer<T, RS> renderer,
	                   RS renderState,
	                   PoseStack poseStack,
	                   SubmitNodeCollector submitNodeCollector,
	                   CameraRenderState cameraRenderState,
	                   Collection<PAnimationController<T>> controllers,
	                   Map<PAnimationController<T>, MolangParser.Context> molangContexts,
	                   int packedColor,
	                   int packedLight,
	                   int packedOverlay)
	{
		submit(renderer, renderState, poseStack, submitNodeCollector, cameraRenderState, controllers, molangContexts, packedColor, packedLight, packedOverlay, null, null);
	}

	public void submit(PEntityRenderer<T, RS> renderer,
	                   RS renderState,
	                   PoseStack poseStack,
	                   SubmitNodeCollector submitNodeCollector,
	                   CameraRenderState cameraRenderState,
	                   Collection<PAnimationController<T>> controllers,
	                   Map<PAnimationController<T>, MolangParser.Context> molangContexts,
	                   int packedColor,
	                   int packedLight,
	                   int packedOverlay,
	                   @Nullable Map<String, Matrix4f> entityBonePoses,
	                   @Nullable Matrix4f layerTransform)
	{
		PBakedModel model = getModel(renderState);
		if (model == null)
			return;
		
		PModelData data = getModelData(renderState);
		for (PBakedBone bone : model.bones())
		{
			renderer.perBoneSubmit(
					renderState,
					poseStack,
					bone,
					controllers,
					data,
					this :: getRenderType,
					packedColor,
					packedLight,
					packedOverlay,
					submitNodeCollector,
					cameraRenderState,
					this,
					entityBonePoses,
					layerTransform,
					molangContexts);
		}
	}
	
	public void setOffset(Vector3f offset)
	{
		this.offset.set(offset);
	}
	
	public Vector3f offset()
	{
		return this.offset;
	}
	
	public void setRotation(Quaternionf rotation)
	{
		this.rotation.set(rotation);
	}
	
	public Quaternionf rotation()
	{
		return this.rotation;
	}
	
	public void setScale(Vector3f scale)
	{
		this.scale.set(scale);
	}
	
	public Vector3f scale()
	{
		return this.scale;
	}
}
