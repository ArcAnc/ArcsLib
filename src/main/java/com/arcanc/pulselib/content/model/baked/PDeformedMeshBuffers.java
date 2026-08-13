/**
 * @author ArcAnc
 * Created at: 08.08.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.model.baked;

import com.arcanc.pulselib.content.model.PMesh;
import com.arcanc.pulselib.content.model.deformer.PMeshDeformation;
import com.arcanc.pulselib.content.model.deformer.PMeshTessellator;
import com.arcanc.pulselib.util.PRenderTypes;
import com.arcanc.pulselib.util.PTextureCache;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import de.javagl.jgltf.model.GltfConstants;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.IdentityHashMap;
import java.util.Map;

public final class PDeformedMeshBuffers
{
	private PDeformedMeshBuffers()
	{
	}

	private static final Map<PBakedMesh, IdentityHashMap<Object, MeshBuffers>> BUFFERS = new IdentityHashMap<>();

	public static MeshBuffers resolve(PBakedMesh mesh, @Nullable PMeshDeformation deformation)
	{
		if (deformation == null || deformation.stack().isEmpty())
			return MeshBuffers.from(mesh);
		IdentityHashMap<Object, MeshBuffers> buffers = BUFFERS.computeIfAbsent(mesh, ignored -> new IdentityHashMap<>());
		MeshBuffers previous = buffers.remove(deformation.cacheKey());
		if (previous != null)
			previous.close();
		MeshBuffers buffersForDeformation = upload(mesh, PMeshTessellator.subdivide(mesh.source(), deformation.subdivisionLevel()), deformation);
		buffers.put(deformation.cacheKey(), buffersForDeformation);
		return buffersForDeformation;
	}

	public static void close(PBakedMesh mesh)
	{
		IdentityHashMap<Object, MeshBuffers> buffers = BUFFERS.remove(mesh);
		if (buffers != null)
			buffers.values().forEach(MeshBuffers::close);
	}

	public static void cleanup()
	{
		BUFFERS.values().forEach(buffers -> buffers.values().forEach(MeshBuffers::close));
		BUFFERS.clear();
	}

	private static MeshBuffers upload(PBakedMesh baked, PMesh mesh, PMeshDeformation deformation)
	{
		TextureAtlasSprite sprite = PTextureCache.getTextureAtlas().getSprite(baked.textureLocation());
		ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(
				mesh.vertexCount() * PRenderTypes.VertexFormatProvider.POSITION_TEX_NORMAL.getVertexSize());
		BufferBuilder builder = sprite.contents().name().getPath().equals("missingno")
				? new BufferBuilder(bytes, VertexFormat.Mode.TRIANGLES, PRenderTypes.VertexFormatProvider.POSITION_TEX_NORMAL)
				: new AtlasBufferBuilder(bytes, VertexFormat.Mode.TRIANGLES, PRenderTypes.VertexFormatProvider.POSITION_TEX_NORMAL, sprite);
		for (int vertex = 0; vertex < mesh.vertexCount(); vertex++)
		{
			Vector3f position = new Vector3f(mesh.positions().get(vertex * 3), mesh.positions().get(vertex * 3 + 1), mesh.positions().get(vertex * 3 + 2));
			Vector3f normal = deformation.stack().deformNormal(position,
					new Vector3f(mesh.normals().get(vertex * 3), mesh.normals().get(vertex * 3 + 1), mesh.normals().get(vertex * 3 + 2)), deformation.values());
			deformation.stack().deformInPlace(position, deformation.values());
			builder.addVertex(position.x, position.y, position.z)
					.setUv(mesh.uvs().get(vertex * 2), mesh.uvs().get(vertex * 2 + 1))
					.setNormal(normal.x, normal.y, normal.z);
		}
		try (MeshData data = builder.buildOrThrow())
		{
			GpuBuffer vertices = RenderSystem.getDevice().createBuffer(
					() -> baked.uuid() + "_deformed",
					GpuBuffer.USAGE_VERTEX,
					data.vertexBuffer());
			ByteBuffer indices = mesh.indices().duplicate();
			indices.clear();
			GpuBuffer indexBuffer = RenderSystem.getDevice().createBuffer(
					() -> baked.uuid() + "_deformed_indices",
					GpuBuffer.USAGE_INDEX,
					indices);
			return new MeshBuffers(vertices, indexBuffer, mesh.indicesCount(), indexType(mesh));
		}
	}

	private static VertexFormat.IndexType indexType(PMesh mesh)
	{
		return switch (mesh.glIndexType())
		{
			case GltfConstants.GL_UNSIGNED_SHORT -> VertexFormat.IndexType.SHORT;
			case GltfConstants.GL_UNSIGNED_INT -> VertexFormat.IndexType.INT;
			default -> throw new IllegalArgumentException("Unsupported GLTF index type: " + mesh.glIndexType());
		};
	}

	public record MeshBuffers(GpuBuffer vertices, GpuBuffer indices, int indicesCount, VertexFormat.IndexType indexType)
	{
		private static MeshBuffers from(PBakedMesh mesh)
		{
			return new MeshBuffers(mesh.vbo(), mesh.indices(), mesh.indicesCount(), mesh.indexType());
		}

		private void close()
		{
			this.vertices.close();
			this.indices.close();
		}
	}
}
