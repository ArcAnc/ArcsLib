/**
 * @author ArcAnc
 * Created at: 13.08.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.renderer.plan;

import java.util.List;

/** Immutable output of {@link PFrameCompiler}. */
public record PRenderPlan(List<PDrawGroup> groups)
{
	public static final PRenderPlan EMPTY = new PRenderPlan(List.of());

	public PRenderPlan
	{
		groups = List.copyOf(groups);
	}

	public boolean isEmpty()
	{
		return this.groups.isEmpty();
	}
}
