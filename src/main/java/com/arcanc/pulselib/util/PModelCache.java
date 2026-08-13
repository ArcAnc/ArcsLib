/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.util;


import com.arcanc.pulselib.content.mixin.VertexBufferAccessor;
import com.arcanc.pulselib.content.model.PBone;
import com.arcanc.pulselib.content.model.PMesh;
import com.arcanc.pulselib.content.model.PModel;
import com.arcanc.pulselib.content.model.baked.AtlasBufferBuilder;
import com.arcanc.pulselib.content.model.baked.PBakedBone;
import com.arcanc.pulselib.content.model.baked.PBakedMesh;
import com.arcanc.pulselib.content.model.baked.PDeformedMeshBuffers;
import com.arcanc.pulselib.content.model.baked.PGpuDeformedMeshBuffers;
import com.arcanc.pulselib.content.model.deformer.gpu.PGpuDeformerBuffers;
import com.arcanc.pulselib.content.model.baked.PBakedModel;
import com.arcanc.pulselib.content.model.textures.atlas.PLibMetadata;
import com.arcanc.pulselib.data.gltf.PGltfModelLoader;
import com.arcanc.pulselib.data.PModelLoader;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PModelCache
{
	private static @Nullable Map<ResourceLocation, PBakedModel> MODELS;
	private static final Map<ResourceLocation, PModelLoader> MODEL_LOADERS = Stream.of(PGltfModelLoader.INSTANCE).
			collect(Collectors.toMap(
			PModelLoader :: id,
			Function.identity(),
			(oldV, newV) ->
			{
				throw new IllegalStateException("Duplicate model loader id: " + oldV.id());
			},
			Object2ObjectOpenHashMap :: new));
	
	public static @Nullable Map<ResourceLocation, PBakedModel> getModels()
	{
		return MODELS;
	}
	
	public static void registerModelLoader(PModelLoader modelLoader)
	{
		if (MODEL_LOADERS.containsKey(modelLoader.id()))
			return;
		
		MODEL_LOADERS.put(modelLoader.id(), modelLoader);
	}
	
	public static void unregisterModelLoader(ResourceLocation loaderId)
	{
		MODEL_LOADERS.remove(loaderId);
	}
	
	public static List<PModelLoader> getModelLoaders()
	{
		return List.copyOf(MODEL_LOADERS.values());
	}
	
	public static Optional<PModelLoader> getModelLoader(ResourceLocation loaderId)
	{
		return Optional.ofNullable(MODEL_LOADERS.get(loaderId));
	}
	
	@ApiStatus.Internal
	public static CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier stage,
	                                             ResourceManager resourceManager,
	                                             ProfilerFiller preparationsProfiler,
	                                             ProfilerFiller reloadProfiler,
	                                             Executor backgroundExecutor,
	                                             Executor gameExecutor)
	{
		Map<ResourceLocation, PModel> models = new Object2ObjectOpenHashMap<>();
		return CompletableFuture.allOf(loadModels(backgroundExecutor, resourceManager, models :: put)).
				thenCompose(stage :: wait).
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
			MODELS.forEach(($, model) ->
					model.bones().forEach(PModelCache :: clearBoneCache));
			MODELS = null;
		}
		PGpuDeformerBuffers.clearDefinitions();
	}
	
	private static void clearBoneCache(PBakedBone bone)
	{
		bone.meshes().forEach(mesh ->
		{
			PDeformedMeshBuffers.close(mesh);
			PGpuDeformedMeshBuffers.close(mesh);
			mesh.vertexBuffer().close();
		});
		bone.children().forEach(PModelCache :: clearBoneCache);
	}
	
	private static Map<ResourceLocation, PBakedModel> bakeModels(Map<ResourceLocation, PModel> rawModels)
	{
		Map<ResourceLocation, PBakedModel> bakedModelMap = new Object2ObjectOpenHashMap<>();
		for (Map.Entry<ResourceLocation, PModel> rawModel : rawModels.entrySet())
		{
			PModel model = rawModel.getValue();
			ResourceLocation modelPath = rawModel.getKey();
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
					ResourceLocation loc = textureLocation(modelPath, mesh.texture());
					
					TextureAtlasSprite sprite = PTextureCache.getTextureAtlas().getSprite(loc);
					boolean isEmissive = sprite.contents().
							metadata().
							getSection(PLibMetadata.TYPE).
							map(PLibMetadata :: isEmissive).
							orElse(false);
					
					
					ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(mesh.vertexCount() * PRenderTypes.VertexFormatProvider.POSITION_TEX_NORMAL.getVertexSize());
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
						VertexBuffer vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
						vertexBuffer.bind();
						vertexBuffer.upload(meshData);
						VertexBufferAccessor accessor = (VertexBufferAccessor)vertexBuffer;
						accessor.pulselib$UploadIndexBuffer(meshData.drawState(), indexBuffer);
						accessor.pulselib$setIndexCount(mesh.indicesCount());
						accessor.pulselib$setIndexType(mesh.glIndexType() == VertexFormat.IndexType.SHORT.asGLType ? VertexFormat.IndexType.SHORT : VertexFormat.IndexType.INT);
						VertexBuffer.unbind();
						builder.meshes.add(new PBakedMesh(
								meshUUID,
								vertexBuffer,
								mesh.texture(),
								isEmissive,
								mesh,
								loc));
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
	
	public static ResourceLocation resolveTextureLocation(ResourceLocation modelPath, String textureName)
	{
		for (PModelLoader modelLoader : PModelCache.getModelLoaders())
			if (modelLoader.supports(modelPath))
				return modelLoader.textureLocation(modelPath, textureName);
		
		return modelPath.withPath(textureName);
	}
	
	private static ResourceLocation textureLocation(ResourceLocation modelPath, String textureName)
	{
		return resolveTextureLocation(modelPath, textureName);
	}
	
	private static CompletableFuture<?> loadModels(Executor backgroundExecutor,
	                                               ResourceManager resourceManager,
	                                               BiConsumer<ResourceLocation, PModel> elementConsumer)
	{
		CompletableFuture<?> chain = CompletableFuture.completedFuture(null);
		
		for (PModelLoader modelLoader : getModelLoaders())
			chain = chain.thenCompose(empty -> modelLoader.loadModels(backgroundExecutor, resourceManager, elementConsumer));
		
		return chain;
	}
}
