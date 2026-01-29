/**
 * @author ArcAnc
 * Created at: 28.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.content.model.baked;


import java.nio.FloatBuffer;
import java.util.UUID;

public record ArcBakedMesh(UUID uuid,
                           FloatBuffer positions,
                           FloatBuffer uvs,
                           FloatBuffer normals,
                           int vertexesAmount,
                           int textureId)
{
}
