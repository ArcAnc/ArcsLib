/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arclib.util;


import com.arcanc.arclib.content.mixin.VertexBufferAccessor;
import com.arcanc.arclib.content.model.ArcBone;
import com.arcanc.arclib.content.model.ArcMesh;
import com.arcanc.arclib.content.model.ArcModel;
import com.arcanc.arclib.content.model.baked.ArcBakedBone;
import com.arcanc.arclib.content.model.baked.ArcBakedMesh;
import com.arcanc.arclib.content.model.baked.ArcBakedModel;
import com.arcanc.arclib.data.ArcModelParser;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Pair;
import de.javagl.jgltf.model.GltfConstants;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

public class ArcModelCache
{
	private static @Nullable Map<ResourceLocation, ArcBakedModel> MODELS;
	
	public static @Nullable Map<ResourceLocation, ArcBakedModel> getModels()
	{
		return MODELS;
	}
	
	@ApiStatus.Internal
	public static CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier stage,
	                                             ResourceManager resourceManager,
	                                             ProfilerFiller preparationsProfiler,
	                                             ProfilerFiller reloadProfiler,
	                                             Executor backgroundExecutor,
	                                             Executor gameExecutor)
			
			
	{
		Map<ResourceLocation, ArcModel> models = new Object2ObjectOpenHashMap<>();
		return CompletableFuture.allOf(loadModels(backgroundExecutor, resourceManager, models :: put)).
				thenCompose(stage :: wait).
				thenAcceptAsync(empty ->
				{
					clearCaches();
					ArcModelCache.MODELS = bakeModels(models);
				},
				gameExecutor);
	}
	
	private static void clearCaches()
	{
		if (MODELS == null)
			return;
		MODELS.forEach((ResourceLocation, model) ->
				model.bones().forEach(ArcModelCache :: clearBoneCache));
		MODELS = null;
	}
	
	private static void clearBoneCache(ArcBakedBone bone)
	{
		bone.meshes().forEach(mesh ->
		{
			mesh.vertexBuffer().close();
		});
		bone.children().forEach(ArcModelCache :: clearBoneCache);
	}
	
	private static Map<ResourceLocation, ArcBakedModel> bakeModels(Map<ResourceLocation, ArcModel> rawModels)
	{
		Map<ResourceLocation, ArcBakedModel> bakedModelMap = new Object2ObjectOpenHashMap<>();
		for (Map.Entry<ResourceLocation, ArcModel> rawModel : rawModels.entrySet())
		{
			ArcModel model = rawModel.getValue();
			Map<UUID, ArcBakedBone.ArcBakedBoneBuilder> bakedBoneBuilder = new HashMap<>();
			for (ArcBone bone : model.bones.values())
			{
				bakedBoneBuilder.put(
						bone.uuid(),
						new ArcBakedBone.ArcBakedBoneBuilder(
								bone.uuid(),
								bone.name(),
								new Vector3f(bone.pivot()),
								new Quaternionf(bone.baseRotation()).normalize()
						)
				);
			}
			
			for (Map.Entry<UUID, Pair<UUID, List<UUID>>> bone2MeshesEntry : model.boneMeshes.entrySet())
			{
				ArcBakedBone.ArcBakedBoneBuilder builder = bakedBoneBuilder.get(bone2MeshesEntry.getKey());
				
				for (UUID meshUUID : bone2MeshesEntry.getValue().getSecond())
				{
					ArcMesh mesh = model.meshes.get(meshUUID);

					ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(mesh.vertexCount() * ArcRenderTypes.VertexFormatProvider.POSITION_TEX_NORMAL.getVertexSize());
					BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.TRIANGLES, ArcRenderTypes.VertexFormatProvider.POSITION_TEX_NORMAL);
					
					for (int q = 0; q < mesh.vertexCount(); q++)
					{
						float x = mesh.positions().get(q * 3);
						float y = mesh.positions().get(q * 3 + 1);
						float z = mesh.positions().get(q * 3 + 2);
						
						float u = mesh.uvs().get(q * 2);
						float v = mesh.uvs().get(q * 2 + 1);
						
						float nx = mesh.normals().get(q * 3);
						float ny = mesh.normals().get(q * 3 + 1);
						float nz = mesh.normals().get(q * 3 + 2);
						
						bufferBuilder.
								addVertex(
										x,
										y,
										z).
								setUv(
										u,
										v).
								setNormal(
										nx,
										ny,
										nz);
					}
					ByteBuffer indexBuffer = mesh.indices();
					
					try (MeshData meshData = bufferBuilder.buildOrThrow())
					{
						VertexBuffer vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
						vertexBuffer.bind();
						vertexBuffer.upload(meshData);
						VertexBufferAccessor accessor = (VertexBufferAccessor)vertexBuffer;
						accessor.arclib$UploadIndexBuffer(meshData.drawState(), indexBuffer);
						accessor.setIndexCount(mesh.indicesCount());
						accessor.setIndexType(mesh.glIndexType() == VertexFormat.IndexType.SHORT.asGLType ? VertexFormat.IndexType.SHORT : VertexFormat.IndexType.INT);
						VertexBuffer.unbind();
						builder.meshes.add(new ArcBakedMesh(
								meshUUID,
								vertexBuffer,
								mesh.texture()));
					}
				}
			}

			for (ArcBone bone : model.bones.values())
			{
				ArcBone parentBone = bone.parent();
				if (parentBone == null)
					continue;
				
				ArcBakedBone.ArcBakedBoneBuilder child =
						bakedBoneBuilder.get(bone.uuid());
				
				ArcBakedBone.ArcBakedBoneBuilder parent =
						bakedBoneBuilder.get(parentBone.uuid());
				
				child.parent = parent;
				parent.children.add(child);
			}

			List<ArcBakedBone> rootBones = new ArrayList<>();
			
			for (ArcBakedBone.ArcBakedBoneBuilder builder : bakedBoneBuilder.values())
				if (builder.parent == null)
					rootBones.add(bakeBone(builder, null));
			
			bakedModelMap.put(
					rawModel.getKey(),
					new ArcBakedModel(ImmutableList.copyOf(rootBones),
									  ImmutableMap.copyOf(model.animations))
			);
		}
		
		return bakedModelMap;
	}
	
	private static ArcBakedBone bakeBone(
			ArcBakedBone.ArcBakedBoneBuilder builder,
			ArcBakedBone bakedParent)
	{
		List<ArcBakedBone> bakedChildren = new ArrayList<>();
		
		ArcBakedBone bakedBone = new ArcBakedBone(
				builder.name,
				builder.basePosition,
				builder.baseRotation,
				List.of(),
				bakedParent,
				ImmutableList.copyOf(builder.meshes)
		);
		
		for (ArcBakedBone.ArcBakedBoneBuilder child : builder.children)
			bakedChildren.add(bakeBone(child, bakedBone));
		
		return new ArcBakedBone(
				builder.name,
				builder.basePosition,
				builder.baseRotation,
				ImmutableList.copyOf(bakedChildren),
				bakedParent,
				ImmutableList.copyOf(builder.meshes)
		);
	}
	
	private static CompletableFuture<?> loadModels(Executor backgroundExecutor,
	                                                        ResourceManager resourceManager,
	                                                        BiConsumer<ResourceLocation, ArcModel> elementConsumer)
	{
		return CompletableFuture.supplyAsync(
				() -> resourceManager.listResources(
						"glmodels",
						fileName -> fileName.toString().endsWith(".glb")),
				backgroundExecutor).
				thenApplyAsync(resources ->
				{
					Map<ResourceLocation, CompletableFuture<ArcModel>> tasks = new Object2ObjectOpenHashMap<>();
					
					for (ResourceLocation resource : resources.keySet())
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
					for (Map.Entry<ResourceLocation, CompletableFuture<ArcModel>> entry : modelsMap.entrySet())
						elementConsumer.accept(entry.getKey(), entry.getValue().join());
				}, backgroundExecutor);
	}
}
