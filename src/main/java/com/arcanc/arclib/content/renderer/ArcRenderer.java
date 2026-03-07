/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arclib.content.renderer;


import com.arcanc.arclib.content.animatable.ArcAnimatable;
import com.arcanc.arclib.content.model.baked.ArcBakedModel;
import com.arcanc.arclib.content.renderer.modelData.ArcModelData;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

public interface ArcRenderer<T extends ArcAnimatable<T>>
{
	ArcModelData getArcModelData();
	
	default ResourceLocation getTextureByName(String name)
	{
		return getArcModelData().getTextureByName(name);
	}
	
	ArcBakedModel getArcModel();
	
	void preRender(PoseStack poseStack, T animatable, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float partialTick);
	void actuallyRender(PoseStack poseStack, T animatable, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float partialTick);
	void postRender(PoseStack poseStack, T animatable, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float partialTick);
}
