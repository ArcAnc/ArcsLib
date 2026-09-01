/**
 * @author ArcAnc
 * Created at: 27.05.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.data;


import com.arcanc.pulselib.content.model.PModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

public interface PModelLoader
{
	ResourceLocation id();

	default void applyItemTransform(PoseStack poseStack)
	{
		poseStack.translate(0.5f, 0, 0.5f);
		poseStack.mulPose(Axis.YP.rotationDegrees(180));
	}
	
	boolean supports(ResourceLocation modelPath);
	
	ResourceLocation defaultModelLocation(ResourceLocation modelLocation, String modelType);
	
	default ResourceLocation defaultTextureLocation(ResourceLocation textureLocation, ResourceLocation modelLocation, String modelType)
	{
		return textureLocation.withPrefix(modelType + "/" + modelLocation.getPath() + "/");
	}
	
	ResourceLocation textureLocation(ResourceLocation modelPath, String textureName);
	
	CompletableFuture<?> loadModels(Executor backgroundExecutor,
	                                ResourceManager resourceManager,
	                                BiConsumer<ResourceLocation, PModel> elementConsumer);
}
