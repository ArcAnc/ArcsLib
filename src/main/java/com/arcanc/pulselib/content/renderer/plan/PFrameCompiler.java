/**
 * @author ArcAnc
 * Created at: 14.08.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.renderer.plan;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;

public final class PFrameCompiler<S, P, M, I>
{
	private final Map<S, Map<DrawKey<P, M>, List<I>>> opaque = new Object2ObjectOpenHashMap<>();
	private final Map<S, List<TransparentSubmission<P, M, I>>> translucent = new Object2ObjectOpenHashMap<>();
	private final Comparator<? super P> pipelineOrder;
	private final ToDoubleFunction<I> distanceSquared;

	public PFrameCompiler(Comparator<? super P> pipelineOrder, ToDoubleFunction<I> distanceSquared)
	{
		this.pipelineOrder = pipelineOrder;
		this.distanceSquared = distanceSquared;
	}

	public void submit(S stage, P pipeline, M mesh, I instance, boolean transparent)
	{
		DrawKey<P, M> key = new DrawKey<>(pipeline, mesh);
		if (transparent)
		{
			this.translucent.computeIfAbsent(stage, ignored -> new ObjectArrayList<>()).add(
					new TransparentSubmission<>(key, instance, this.distanceSquared.applyAsDouble(instance)));
			return;
		}
		this.opaque.computeIfAbsent(stage, ignored -> new Object2ObjectOpenHashMap<>()).
				computeIfAbsent(key, ignored -> new ObjectArrayList<>()).add(instance);
	}

	public PRenderPlan<P, M, I> compile(S stage)
	{
		Map<DrawKey<P, M>, List<I>> opaqueGroups = this.opaque.remove(stage);
		List<TransparentSubmission<P, M, I>> translucentGroups = this.translucent.remove(stage);
		if (opaqueGroups == null && translucentGroups == null)
			return PRenderPlan.empty();

		List<PDrawGroup<P, M, I>> groups = new ArrayList<>();
		if (opaqueGroups != null)
			for (Map.Entry<DrawKey<P, M>, List<I>> entry : opaqueGroups.entrySet())
				groups.add(group(entry.getKey(), entry.getValue(), true));
		groups.sort(Comparator.comparing(PDrawGroup<P, M, I>::pipeline, this.pipelineOrder));

		if (translucentGroups != null && !translucentGroups.isEmpty())
		{
			translucentGroups.sort(Comparator.comparingDouble(TransparentSubmission<P, M, I>::distanceSquared).reversed());
			DrawKey<P, M> activeKey = null;
			List<I> activeInstances = new ObjectArrayList<>();
			for (TransparentSubmission<P, M, I> submission : translucentGroups)
			{
				if (activeKey != null && !activeKey.equals(submission.key()))
				{
					groups.add(group(activeKey, activeInstances, false));
					activeInstances = new ObjectArrayList<>();
				}
				activeKey = submission.key();
				activeInstances.add(submission.instance());
			}
			if (activeKey != null)
				groups.add(group(activeKey, activeInstances, false));
		}

		return groups.isEmpty() ? PRenderPlan.empty() : new PRenderPlan<>(groups);
	}

	public void clear()
	{
		this.opaque.clear();
		this.translucent.clear();
	}

	private static <P, M, I> PDrawGroup<P, M, I> group(DrawKey<P, M> key, List<I> instances, boolean writeDepth)
	{
		return new PDrawGroup<>(key.pipeline(), key.mesh(), writeDepth, instances);
	}

	private record DrawKey<P, M>(P pipeline, M mesh)
	{
	}

	private record TransparentSubmission<P, M, I>(DrawKey<P, M> key, I instance, double distanceSquared)
	{
	}
}
