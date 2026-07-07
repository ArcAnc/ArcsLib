/**
 * @author ArcAnc
 * Created at: 28.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.renderer;


import com.arcanc.pulselib.content.animatable.PAnimatable;
import com.arcanc.pulselib.content.animatable.PAnimationController;
import com.arcanc.pulselib.content.animatable.PAnimationManager;
import com.arcanc.pulselib.content.mixin.ItemStackRenderStateAccessor;
import com.arcanc.pulselib.content.model.animation.BoneFrame;
import com.arcanc.pulselib.content.model.baked.PBakedBone;
import com.arcanc.pulselib.content.model.baked.PBakedMesh;
import com.arcanc.pulselib.content.model.baked.PBakedModel;
import com.arcanc.pulselib.content.model.baked.PMeshRenderContext;
import com.arcanc.pulselib.content.renderer.base.PItemRenderState;
import com.arcanc.pulselib.content.renderer.modelData.PModelData;
import com.arcanc.pulselib.util.PRenderTypes;
import com.arcanc.pulselib.util.PTextureCache;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class PItemRenderer<T extends Item & PAnimatable<T>, RS extends PItemRenderState<T>> implements SpecialModelRenderer<RS>, PRenderer<T, RS>
{
	private final PModelData modelData;
	private final Function<Identifier, RenderType> renderType;
	
	public PItemRenderer(PModelData modelData, Function<Identifier, RenderType> renderType)
	{
		this.modelData = modelData;
		this.renderType = renderType;
	}
	
	protected abstract RS createRenderState();
	
	@Override
	public void submit(@Nullable RS renderState,
	                   PoseStack poseStack,
	                   SubmitNodeCollector submitNodeCollector,
	                   int lightCoords,
	                   int overlayCoords,
	                   boolean hasFoil,
	                   int outlineColor)
	{
		if (renderState == null)
			return;
		renderState.extractAdditionalData(lightCoords,  overlayCoords, hasFoil, outlineColor);
		poseStack.pushPose();
		poseStack.translate(0.5f, 0, 0.5f);
		poseStack.mulPose(Axis.YP.rotationDegrees(180));
		
		CameraRenderState cameraRenderState = new CameraRenderState();
		preSubmit(poseStack, renderState, cameraRenderState, submitNodeCollector);
		trueSubmit(poseStack, renderState, cameraRenderState, submitNodeCollector);
		postSubmit(poseStack, renderState, cameraRenderState, submitNodeCollector);
		poseStack.popPose();
	}
	
	@Override
	public void getExtents(Consumer<Vector3fc> output)
	{
		output.accept(new Vector3f());
	}
	
	@Override
	public @Nullable RS extractArgument(ItemStack stack)
	{
		RS state = createRenderState();
		state.extractStackData(stack, this);
		return state;
	}
	
	@Override
	public PModelData getModelData(RS renderState)
	{
		return this.modelData;
	}
	
	@Override
	public @Nullable PBakedModel getModel(RS renderState)
	{
		return getModelData(renderState).getModel();
	}
	
	@Override
	public RenderType getRenderType(Identifier texture)
	{
		return this.renderType.apply(texture);
	}
	
	@Override
	public void preSubmit(PoseStack poseStack, RS renderState, CameraRenderState cameraRenderState, SubmitNodeCollector submitNodeCollector)
	{
	}
	
	@Override
	public void trueSubmit(PoseStack poseStack, RS renderState, CameraRenderState cameraRenderState, SubmitNodeCollector submitNodeCollector)
	{
		PBakedModel model = renderState.getBakedModel();
		if (model == null)
			return;
		
		PAnimationManager<T> manager = renderState.getAnimatable().getAnimationManager(renderState.getAnimKey());
		manager.bindModel(model);
		
		Collection<PAnimationController<T>> controllers = manager.getControllers().values();
		
		ItemDisplayContext context = ((ItemStackRenderStateAccessor)renderState.itemRenderState()).pulselib$getDisplayContext();
		if (context == ItemDisplayContext.GUI)
		{
			model.bones().forEach(bone -> perBoneSubmit(renderState, poseStack, bone, controllers, renderType, -1, renderState.lightCoords(), renderState.overlayCoords(), context));
			submitNodeCollector.submitCustomGeometry(
					poseStack,
					this.renderType.apply(PTextureCache.ATLAS_LOCATION),
					(_, _) -> PRenderQueue.flush(PRenderQueue.RenderStage.GUI));
			return;
		}
		model.bones().forEach(bone -> perBoneSubmit(renderState, poseStack, bone, controllers, renderType, -1, renderState.lightCoords(), renderState.overlayCoords(), context));
	}
	
	@Override
	public void postSubmit(PoseStack poseStack, RS renderState, CameraRenderState cameraRenderState, SubmitNodeCollector submitNodeCollector)
	{
	}
	
	protected void perBoneSubmit(RS renderState, PoseStack poseStack, PBakedBone bone, Collection<PAnimationController<T>> controllers, Function<Identifier, RenderType> renderType, int packedColor, int packedLight, int packedOverlay, ItemDisplayContext context)
	{
		PModelData data = this.getModelData(renderState);
		PBakedModel model = data.getModel();
		if (model == null)
			return;
		BoneFrame frame = bone.mixBone(model, controllers, renderState.partialTick());
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
		
		this.submitBone(renderState, bone, poseStack, data, controllers, renderType, packedColor, packedLight, packedOverlay, context);
		
		if (!bone.children().isEmpty())
			bone.children().forEach(child -> perBoneSubmit(renderState, poseStack, child, controllers, renderType, packedColor, packedLight, packedOverlay, context));
		
		poseStack.popPose();
	}
	
	protected void submitBone(RS renderState,
	                          PBakedBone bone,
	                          PoseStack poseStack,
	                          PModelData modelData,
	                          Collection<PAnimationController<T>> controllers,
	                          Function<Identifier, RenderType> renderType,
	                          int color,
	                          int packedLight,
	                          int packedOverlay,
	                          ItemDisplayContext context)
	{
		Matrix4f matrix4fstack = new Matrix4f(poseStack.last().pose());
		
		bone.meshes().forEach(mesh ->
		{
			if (mesh.textureName().isEmpty())
				return;
			
			PMeshRenderContext inherited = new PMeshRenderContext(
					renderType,
					color,
					mesh.isEmissive() ? LightCoordsUtil.FULL_BRIGHT : packedLight,
					packedOverlay);
			PMeshRenderContext meshContext = resolveMeshRender(renderState, context, bone, mesh, inherited);
			
			RenderType baseType = meshContext.renderType().apply(PTextureCache.ATLAS_LOCATION);
			RenderType type = mesh.isEmissive() ?
					PRenderTypes.RenderTypeProvider.emissiveVariant(baseType, PTextureCache.ATLAS_LOCATION) :
					baseType;
			
			PRenderQueue.submitItem(context, type, mesh, new PRenderQueue.InstanceData(matrix4fstack, meshContext.color(), meshContext.packedLight(), meshContext.packedOverlay()));
		});
	}
	
	protected PMeshRenderContext resolveMeshRender(RS renderState,
	                                               ItemDisplayContext context,
	                                               PBakedBone bone,
	                                               PBakedMesh mesh,
	                                               PMeshRenderContext inherited)
	{
		return inherited;
	}
}
