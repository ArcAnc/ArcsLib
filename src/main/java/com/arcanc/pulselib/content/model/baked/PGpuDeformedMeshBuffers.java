/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.model.baked;

import com.arcanc.pulselib.content.mixin.VertexBufferAccessor;
import com.arcanc.pulselib.content.model.PMesh;
import com.arcanc.pulselib.content.model.deformer.PMeshTessellator;
import com.arcanc.pulselib.util.PRenderTypes;
import com.arcanc.pulselib.util.PTextureCache;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.nio.ByteBuffer;
import java.util.IdentityHashMap;
import java.util.Map;

public final class PGpuDeformedMeshBuffers
{
	private static final Map<PBakedMesh, Map<Integer, VertexBuffer>> BUFFERS = new IdentityHashMap<>();

	private PGpuDeformedMeshBuffers()
	{
	}

	public static VertexBuffer resolve(PBakedMesh mesh, int subdivisionLevel)
	{
		if (subdivisionLevel == 0)
			return mesh.vertexBuffer();
		return BUFFERS.computeIfAbsent(mesh, ignored -> new java.util.HashMap<>()).computeIfAbsent(
				subdivisionLevel, level -> bake(mesh, PMeshTessellator.subdivide(mesh.source(), level)));
	}

	public static void close(PBakedMesh mesh)
	{
		Map<Integer, VertexBuffer> buffers = BUFFERS.remove(mesh);
		if (buffers != null)
			buffers.values().forEach(VertexBuffer :: close);
	}

	public static void cleanup()
	{
		for (Map<Integer, VertexBuffer> buffers : BUFFERS.values())
			buffers.values().forEach(VertexBuffer :: close);
		BUFFERS.clear();
	}

	private static VertexBuffer bake(PBakedMesh baked, PMesh mesh)
	{
		TextureAtlasSprite sprite = PTextureCache.getTextureAtlas().getSprite(baked.textureLocation());
		ByteBufferBuilder bytes = new ByteBufferBuilder(mesh.vertexCount() * PRenderTypes.VertexFormatProvider.POSITION_TEX_NORMAL.getVertexSize());
		BufferBuilder builder = sprite.contents().name().getPath().equals("missingno") ?
				new BufferBuilder(bytes, VertexFormat.Mode.TRIANGLES, PRenderTypes.VertexFormatProvider.POSITION_TEX_NORMAL) :
				new AtlasBufferBuilder(bytes, VertexFormat.Mode.TRIANGLES, PRenderTypes.VertexFormatProvider.POSITION_TEX_NORMAL, sprite);
		for (int vertex = 0; vertex < mesh.vertexCount(); vertex++)
			builder.addVertex(mesh.positions().get(vertex * 3), mesh.positions().get(vertex * 3 + 1), mesh.positions().get(vertex * 3 + 2)).
					setUv(mesh.uvs().get(vertex * 2), mesh.uvs().get(vertex * 2 + 1)).
					setNormal(mesh.normals().get(vertex * 3), mesh.normals().get(vertex * 3 + 1), mesh.normals().get(vertex * 3 + 2));
		VertexBuffer result = new VertexBuffer(VertexBuffer.Usage.STATIC);
		try (MeshData data = builder.buildOrThrow())
		{
			result.bind();
			result.upload(data);
			VertexBufferAccessor accessor = (VertexBufferAccessor)result;
			ByteBuffer indices = mesh.indices().duplicate();
			indices.clear();
			accessor.pulselib$UploadIndexBuffer(data.drawState(), indices);
			accessor.pulselib$setIndexCount(mesh.indicesCount());
			accessor.pulselib$setIndexType(mesh.glIndexType() == VertexFormat.IndexType.SHORT.asGLType ? VertexFormat.IndexType.SHORT : VertexFormat.IndexType.INT);
			VertexBuffer.unbind();
		}
		return result;
	}
}
