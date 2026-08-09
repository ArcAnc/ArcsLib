/**
 * @author ArcAnc
 * Created at: 28.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.model.baked;


<<<<<<< HEAD
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.IndexType;

import java.util.UUID;

public record PBakedMesh(UUID uuid,
                         GpuBuffer vbo,
                         int vertexesAmount,
                         GpuBuffer indices,
                         int indicesCount,
                         IndexType indexType,
                         String textureName,
                         boolean isEmissive)
=======
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.arcanc.pulselib.content.model.PMesh;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record PBakedMesh(
		UUID uuid,
		VertexBuffer vertexBuffer,
		String textureName,
		boolean isEmissive,
		PMesh source,
		ResourceLocation textureLocation)
>>>>>>> a625c91 (Added deformers for player and custom models)
{
}
