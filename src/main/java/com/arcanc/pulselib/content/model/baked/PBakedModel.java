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
import com.arcanc.pulselib.content.model.animation.PAnimation;
import com.arcanc.pulselib.data.MolangParser;
import com.arcanc.pulselib.content.renderer.modelData.PModelData;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public record PBakedModel(List<PBakedBone> bones, Map<String, PAnimation> animations)
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
		instantDraw(poseStack, modelData, controllers, Map.of(), renderType, color, packedLight, packedOverlay, partialTick);
	}

	public <T extends PAnimatable<T>>void instantDraw(PoseStack poseStack,
	                                                  PModelData modelData,
	                                                  Collection<PAnimationController<T>> controllers,
	                                                  Map<PAnimationController<T>, MolangParser.Context> molangContexts,
	                                                  Function<ResourceLocation, RenderType> renderType,
	                                                  int color,
	                                                  int packedLight,
	                                                  int packedOverlay,
	                                                  float partialTick)
	{
		this.bones.forEach(bone -> bone.instantDraw(
				poseStack,
				modelData,
				controllers,
				molangContexts,
				renderType,
				color,
				packedLight,
				packedOverlay,
				partialTick));
	}
	
	public <T extends PAnimatable<T>>void instantDraw(PoseStack poseStack,
	                                                  PModelData modelData,
	                                                  Collection<PAnimationController<T>> controllers,
	                                                  PMeshRenderResolver resolver,
	                                                  PMeshRenderContext inherited,
	                                                  float partialTick)
	{
		instantDraw(poseStack, modelData, controllers, Map.of(), resolver, inherited, partialTick);
	}

	public <T extends PAnimatable<T>>void instantDraw(PoseStack poseStack,
	                                                  PModelData modelData,
	                                                  Collection<PAnimationController<T>> controllers,
	                                                  Map<PAnimationController<T>, MolangParser.Context> molangContexts,
	                                                  PMeshRenderResolver resolver,
	                                                  PMeshRenderContext inherited,
	                                                  float partialTick)
	{
		this.bones.forEach(bone -> bone.instantDraw(
				poseStack,
				modelData,
				controllers,
				molangContexts,
				resolver,
				inherited,
				partialTick));
	}
}
