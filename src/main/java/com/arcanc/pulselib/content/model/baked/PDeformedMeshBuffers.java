/**
 * @author ArcAnc
 * Created at: 08.08.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.model.baked;

/**
 * Lifecycle hook for temporary deformed mesh buffers.
 *
 * Minecraft 26.1 renders baked meshes through immutable {@code GpuBuffer}s,
 * rather than the mutable {@code VertexBuffer} API used by 1.21.1.  Player
 * deformation is performed by {@code PDeformedCuboid}; custom-model buffer
 * replacement is intentionally deferred until the new GPU upload path is
 * available.
 */
public final class PDeformedMeshBuffers
{
	private PDeformedMeshBuffers()
	{
	}

	public static void close(PBakedMesh mesh)
	{
	}

	public static void cleanup()
	{
	}
}
