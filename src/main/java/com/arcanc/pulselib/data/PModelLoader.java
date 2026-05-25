/**
 * @author ArcAnc
 * Created at: 24.05.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.data;


import com.arcanc.pulselib.content.model.PModel;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

public interface PModelLoader
{
	Identifier id();
	
	boolean supports(Identifier modelPath);
	
	Identifier defaultModelLocation(Identifier modelLocation, String modelType);
	
	default Identifier defaultTextureLocation(Identifier textureLocation, Identifier modelLocation, String modelType)
	{
		return textureLocation.withPrefix(modelType + "/" + modelLocation.getPath() + "/");
	}
	
	Identifier textureLocation(Identifier modelPath, String textureName);
	
	CompletableFuture<?> loadModels(Executor backgroundExecutor,
	                                ResourceManager resourceManager,
	                                BiConsumer<Identifier, PModel> elementConsumer);
}
