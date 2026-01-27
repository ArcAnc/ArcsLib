/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.util;


import com.arcanc.arcslib.content.model.ArcModel;
import com.arcanc.arcslib.data.ArcModelParser;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

public class ArcModelCache
{
	private static Map<Identifier, ArcModel> MODELS;
	
	public static Map<Identifier, ArcModel> getModels()
	{
		return MODELS;
	}
	
	@ApiStatus.Internal
	public static @NotNull CompletableFuture<Void> reload(PreparableReloadListener.@NotNull SharedState sharedState,
	                                                      Executor backgroundExecutor,
	                                                      PreparableReloadListener.@NotNull PreparationBarrier preparationBarrier,
	                                                      Executor gameExecutor)
	{
		Map<Identifier, ArcModel> models = new Object2ObjectOpenHashMap<>();
		return CompletableFuture.allOf(loadModels(backgroundExecutor, sharedState.resourceManager(), models :: put)).
				thenCompose(preparationBarrier :: wait).
				thenAcceptAsync(empty ->
						ArcModelCache.MODELS = models,
				gameExecutor);
	}
	
	private static @NotNull CompletableFuture<?> loadModels(Executor backgroundExecutor,
	                                                        ResourceManager resourceManager,
	                                                        BiConsumer<Identifier, ArcModel> elementConsumer)
	{
		return CompletableFuture.supplyAsync(
				() -> resourceManager.listResources(
						"bbmodels",
						fileName -> fileName.toString().endsWith(".bbmodel")),
				backgroundExecutor).
				thenApplyAsync(resources ->
				{
					Map<Identifier, CompletableFuture<ArcModel>> tasks = new Object2ObjectOpenHashMap<>();
					
					for (Identifier resource : resources.keySet())
					{
						tasks.put(resource, CompletableFuture.supplyAsync(() ->
						{
							ArcModel model;
							try
							{
								model = ArcModelParser.parse(resources.get(resource).open());
							}
							catch (IOException e)
							{
								throw new RuntimeException("Can't load model " + e);
							}
							return model;
						}));
					}
					return tasks;
				}, backgroundExecutor).
				thenAcceptAsync(modelsMap ->
				{
					for (Map.Entry<Identifier, CompletableFuture<ArcModel>> entry : modelsMap.entrySet())
						elementConsumer.accept(entry.getKey(), entry.getValue().join());
				}, backgroundExecutor);
	}
}
