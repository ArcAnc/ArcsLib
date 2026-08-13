/**
 * @author ArcAnc
 * Created at: 08.08.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.model.baked;

import com.arcanc.pulselib.content.mixin.VertexBufferAccessor;
import com.arcanc.pulselib.content.model.PMesh;
import com.arcanc.pulselib.content.model.deformer.PMeshDeformation;
import com.arcanc.pulselib.content.model.deformer.PMeshTessellator;
import com.arcanc.pulselib.content.renderer.legacy.GlDynamicGeometry;
import com.arcanc.pulselib.util.PRenderTypes;
import com.arcanc.pulselib.util.PTextureCache;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.util.IdentityHashMap;
import java.util.Map;

public final class PDeformedMeshBuffers
{
	private static final Map<PBakedMesh, IdentityHashMap<Object, GlDynamicGeometry>> BUFFERS = new IdentityHashMap<>();
	private static final Map<PBakedMesh, Map<Integer, PMesh>> SUBDIVIDED_SOURCES = new IdentityHashMap<>();

	private PDeformedMeshBuffers()
	{
	}

	public static GlDynamicGeometry resolve(PBakedMesh mesh, PMeshDeformation deformation)
	{
		if (deformation == null || deformation.stack().isEmpty())
			throw new IllegalArgumentException("Static meshes must be submitted through their Geometry Arena data");
		GlDynamicGeometry geometry = BUFFERS.computeIfAbsent(mesh, ignored -> new IdentityHashMap<>()).computeIfAbsent(
				deformation.cacheKey(), ignored -> new GlDynamicGeometry());
		upload(geometry.vertexBuffer(), mesh, source(mesh, deformation), deformation);
		return geometry;
	}

	public static void close(PBakedMesh mesh)
	{
		SUBDIVIDED_SOURCES.remove(mesh);
		IdentityHashMap<Object, GlDynamicGeometry> buffers = BUFFERS.remove(mesh);
		if (buffers != null)
			buffers.values().forEach(GlDynamicGeometry::close);
	}

	public static void cleanup()
	{
		for (IdentityHashMap<Object, GlDynamicGeometry> buffers : BUFFERS.values())
			buffers.values().forEach(GlDynamicGeometry::close);
		BUFFERS.clear();
		SUBDIVIDED_SOURCES.clear();
	}

	private static PMesh source(PBakedMesh mesh, PMeshDeformation deformation)
	{
		int level = deformation.subdivisionLevel();
		if (level == 0)
			return mesh.source();
		return SUBDIVIDED_SOURCES.computeIfAbsent(mesh, ignored -> new java.util.HashMap<>()).computeIfAbsent(
				level, ignored -> PMeshTessellator.subdivide(mesh.source(), level));
	}

	private static void upload(VertexBuffer target, PBakedMesh baked, PMesh mesh, PMeshDeformation deformation)
	{
		TextureAtlasSprite sprite = PTextureCache.getTextureAtlas().getSprite(baked.textureLocation());
		ByteBufferBuilder bytes = new ByteBufferBuilder(mesh.vertexCount() * PRenderTypes.VertexFormatProvider.POSITION_TEX_NORMAL.getVertexSize());
		BufferBuilder builder = sprite.contents().name().getPath().equals("missingno") ?
				new BufferBuilder(bytes, VertexFormat.Mode.TRIANGLES, PRenderTypes.VertexFormatProvider.POSITION_TEX_NORMAL) :
				new AtlasBufferBuilder(bytes, VertexFormat.Mode.TRIANGLES, PRenderTypes.VertexFormatProvider.POSITION_TEX_NORMAL, sprite);
		for (int vertex = 0; vertex < mesh.vertexCount(); vertex++)
		{
			Vector3f position = new Vector3f(mesh.positions().get(vertex * 3), mesh.positions().get(vertex * 3 + 1), mesh.positions().get(vertex * 3 + 2));
			Vector3f normal = deformation.stack().deformNormal(position,
					new Vector3f(mesh.normals().get(vertex * 3), mesh.normals().get(vertex * 3 + 1), mesh.normals().get(vertex * 3 + 2)), deformation.values());
			deformation.stack().deformInPlace(position, deformation.values());
			builder.addVertex(position.x, position.y, position.z).
							setUv(mesh.uvs().get(vertex * 2), mesh.uvs().get(vertex * 2 + 1)).
							setNormal(normal.x, normal.y, normal.z);
		}
		try (MeshData data = builder.buildOrThrow())
		{
			target.bind();
			target.upload(data);
			VertexBufferAccessor accessor = (VertexBufferAccessor)target;
			ByteBuffer indices = mesh.indices().duplicate();
			indices.clear();
			accessor.pulselib$UploadIndexBuffer(data.drawState(), indices);
			accessor.pulselib$setIndexCount(mesh.indicesCount());
			accessor.pulselib$setIndexType(mesh.glIndexType() == VertexFormat.IndexType.SHORT.asGLType ? VertexFormat.IndexType.SHORT : VertexFormat.IndexType.INT);
			VertexBuffer.unbind();
		}
	}
}
