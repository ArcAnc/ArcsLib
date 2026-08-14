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

public record PRenderPlan<P, M, I>(List<PDrawGroup<P, M, I>> groups)
{
	public static final PRenderPlan<?, ?, ?> EMPTY = new PRenderPlan<>(List.of());

	public PRenderPlan
	{
		groups = List.copyOf(groups);
	}

	public boolean isEmpty()
	{
		return this.groups.isEmpty();
	}

	@SuppressWarnings("unchecked")
	public static <P, M, I> PRenderPlan<P, M, I> empty()
	{
		return (PRenderPlan<P, M, I>) EMPTY;
	}
}
