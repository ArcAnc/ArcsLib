/**
 * @author ArcAnc
 * Created at: 14.08.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.renderer.plan;

import java.util.List;

public record PDrawGroup<P, M, I>(P pipeline, M mesh, boolean writeDepth, List<I> instances)
{
	public PDrawGroup
	{
		instances = List.copyOf(instances);
	}
}
