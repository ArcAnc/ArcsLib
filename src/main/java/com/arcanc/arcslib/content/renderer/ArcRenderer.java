/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.content.renderer;


import com.arcanc.arcslib.content.animatable.ArcAnimatable;
import com.arcanc.arcslib.content.model.baked.ArcBakedModel;
import com.arcanc.arcslib.content.renderer.base.ArcRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.Identifier;

public interface ArcRenderer<T extends ArcAnimatable<T>, RS extends ArcRenderState<T>>
{
	ArcModelData getArcModelData();
	
	default Identifier getTextureByName(String name)
	{
		return getArcModelData().getTextureByName(name);
	}
	
	ArcBakedModel getArcModel();
	
	void preRender(PoseStack poseStack, RS renderState, CameraRenderState cameraRenderState, SubmitNodeCollector submitNodeCollector);
	void actuallyRender(PoseStack poseStack, RS renderState, CameraRenderState cameraRenderState, SubmitNodeCollector submitNodeCollector);
	void postRender(PoseStack poseStack, RS renderState, CameraRenderState cameraRenderState, SubmitNodeCollector submitNodeCollector);
}
