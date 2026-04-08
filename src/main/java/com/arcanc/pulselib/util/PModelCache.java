/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.util;


import com.arcanc.pulselib.content.model.PBone;
import com.arcanc.pulselib.content.model.PMesh;
import com.arcanc.pulselib.content.model.PModel;
import com.arcanc.pulselib.content.model.baked.AtlasBufferBuilder;
import com.arcanc.pulselib.content.model.baked.PBakedBone;
import com.arcanc.pulselib.content.model.baked.PBakedMesh;
import com.arcanc.pulselib.content.model.baked.PBakedModel;
import com.arcanc.pulselib.data.PModelParser;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import de.javagl.jgltf.model.GltfConstants;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

public class PModelCache
{
	private static @Nullable Map<Identifier, PBakedModel> MODELS;
	
	public static @Nullable Map<Identifier, PBakedModel> getModels()
	{
		return MODELS;
	}
	
	@ApiStatus.Internal
	public static CompletableFuture<Void> reload(PreparableReloadListener.SharedState sharedState,
	                                             Executor backgroundExecutor,
	                                             PreparableReloadListener.PreparationBarrier preparationBarrier,
	                                             Executor gameExecutor)
	{
		Map<Identifier, PModel> models = new Object2ObjectOpenHashMap<>();
		return CompletableFuture.allOf(loadModels(backgroundExecutor, sharedState.resourceManager(), models :: put)).
				thenCompose(preparationBarrier :: wait).
				thenAcceptAsync(empty ->
				{
					if (PModelCache.MODELS != null)
						clearCaches();
					PModelCache.MODELS = bakeModels(models);
				},
				gameExecutor);
	}
	
	private static void clearCaches()
	{
		if (MODELS != null)
		{
			MODELS.forEach((_, model) ->
				model.bones().forEach(PModelCache :: clearBoneCache));
			MODELS = null;
		}
	}
	
	private static void clearBoneCache(PBakedBone bone)
	{
		bone.meshes().forEach(mesh ->
		{
			mesh.vbo().close();
			mesh.indices().close();
		});
		bone.children().forEach(PModelCache :: clearBoneCache);
	}
	
	private static Map<Identifier, PBakedModel> bakeModels(Map<Identifier, PModel> rawModels)
	{
		Map<Identifier, PBakedModel> bakedModelMap = new Object2ObjectOpenHashMap<>();
		for (Map.Entry<Identifier, PModel> rawModel : rawModels.entrySet())
		{
			PModel model = rawModel.getValue();
			Identifier modelPath = rawModel.getKey();
			String[] divided = modelPath.getPath().substring(0, modelPath.getPath().length() - 4).split("/");
			Map<UUID, PBakedBone.PBakedBoneBuilder> bakedBoneBuilder = new HashMap<>();
			for (PBone bone : model.bones.values())
			{
				bakedBoneBuilder.put(
						bone.uuid(),
						new PBakedBone.PBakedBoneBuilder(
								bone.uuid(),
								bone.name(),
								new Vector3f(bone.pivot()),
								new Quaternionf(bone.baseRotation()).normalize()
						)
				);
			}
			
			for (Map.Entry<UUID, Pair<UUID, List<UUID>>> bone2MeshesEntry : model.boneMeshes.entrySet())
			{
				PBakedBone.PBakedBoneBuilder builder = bakedBoneBuilder.get(bone2MeshesEntry.getKey());
				
				for (UUID meshUUID : bone2MeshesEntry.getValue().getSecond())
				{
					PMesh mesh = model.meshes.get(meshUUID);
					Identifier loc = modelPath.withPath(divided[1] + "/" + divided[2] + "/" + mesh.texture());
					TextureAtlasSprite sprite = PTextureCache.getTextureAtlas().getSprite(loc);
					
					ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.
							exactlySized(mesh.vertexCount() * PRenderTypes.VertexFormatProvider.POSITION_TEX_NORMAL.getVertexSize());
					BufferBuilder bufferBuilder;
					
					if (sprite.contents().name().getPath().equals("missingno"))
						bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.TRIANGLES, PRenderTypes.VertexFormatProvider.POSITION_TEX_NORMAL);
					else
						bufferBuilder = new AtlasBufferBuilder(byteBufferBuilder, VertexFormat.Mode.TRIANGLES, PRenderTypes.VertexFormatProvider.POSITION_TEX_NORMAL, sprite);
					
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
						GpuBuffer buffer = RenderSystem.getDevice().createBuffer(
								meshUUID :: toString,
								GpuBuffer.USAGE_VERTEX,
								meshData.vertexBuffer()
						);
						
						GpuBuffer gpuIndexBuffer = RenderSystem.getDevice().createBuffer(
								() -> meshUUID.toString() + "_indexes",
								GpuBuffer.USAGE_INDEX,
								indexBuffer);
						VertexFormat.IndexType type = mesh.glIndexType() == GltfConstants.GL_UNSIGNED_SHORT ? VertexFormat.IndexType.SHORT : VertexFormat.IndexType.INT;
						builder.meshes.add(new PBakedMesh(
								meshUUID,
								buffer,
								mesh.vertexCount(),
								gpuIndexBuffer,
								mesh.indicesCount(),
								type,
								mesh.texture()));
					}
				}
			}

			for (PBone bone : model.bones.values())
			{
				PBone parentBone = bone.parent();
				if (parentBone == null)
					continue;
				
				PBakedBone.PBakedBoneBuilder child =
						bakedBoneBuilder.get(bone.uuid());
				
				PBakedBone.PBakedBoneBuilder parent =
						bakedBoneBuilder.get(parentBone.uuid());
				
				child.parent = parent;
				parent.children.add(child);
			}

			List<PBakedBone> rootBones = new ArrayList<>();
			
			for (PBakedBone.PBakedBoneBuilder builder : bakedBoneBuilder.values())
				if (builder.parent == null)
					rootBones.add(bakeBone(builder, null));
			
			bakedModelMap.put(
					rawModel.getKey(),
					new PBakedModel(ImmutableList.copyOf(rootBones),
									  ImmutableMap.copyOf(model.animations))
			);
		}
		
		return bakedModelMap;
	}
	
	private static PBakedBone bakeBone(
			PBakedBone.PBakedBoneBuilder builder,
			@Nullable PBakedBone bakedParent)
	{
		List<PBakedBone> bakedChildren = new ArrayList<>();
		
		PBakedBone bakedBone = new PBakedBone(
				builder.name,
				builder.basePosition,
				builder.baseRotation,
				List.of(),
				bakedParent,
				ImmutableList.copyOf(builder.meshes)
		);
		
		for (PBakedBone.PBakedBoneBuilder child : builder.children)
			bakedChildren.add(bakeBone(child, bakedBone));
		
		return new PBakedBone(
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
	                                               BiConsumer<Identifier, PModel> elementConsumer)
	{
		return CompletableFuture.supplyAsync(
				() -> resourceManager.listResources(
						"glmodels",
						fileName -> fileName.toString().endsWith(".glb")),
				backgroundExecutor).
				thenApplyAsync(resources ->
				{
					Map<Identifier, CompletableFuture<PModel>> tasks = new Object2ObjectOpenHashMap<>();
					
					for (Identifier resource : resources.keySet())
					{
						tasks.put(resource, CompletableFuture.supplyAsync(() ->
						{
							PModel model;
							try
							{
								model = PModelParser.parse(resources.get(resource).open());
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
					for (Map.Entry<Identifier, CompletableFuture<PModel>> entry : modelsMap.entrySet())
						elementConsumer.accept(entry.getKey(), entry.getValue().join());
				}, backgroundExecutor);
	}
}
