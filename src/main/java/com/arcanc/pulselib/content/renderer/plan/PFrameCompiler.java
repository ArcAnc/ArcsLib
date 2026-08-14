/**
 * @author ArcAnc
 * Created at: 13.08.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.renderer.plan;

import com.arcanc.pulselib.content.model.baked.PBakedMesh;
import com.arcanc.pulselib.content.renderer.PRenderQueue;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Compiles submissions for one render stage into an immutable frame plan.
 * Opaque geometry may be batched freely; transparent geometry retains its
 * back-to-front ordering and is only batched across adjacent equal draws.
 */
public final class PFrameCompiler<S>
{
	private final Map<S, Map<DrawKey, List<PRenderQueue.InstanceData>>> opaque = new Object2ObjectOpenHashMap<>();
	private final Map<S, List<TransparentSubmission>> translucent = new Object2ObjectOpenHashMap<>();

	public void submit(S stage, RenderType pipeline, PBakedMesh mesh, PRenderQueue.InstanceData instance, boolean transparent)
	{
		DrawKey key = new DrawKey(pipeline, mesh);
		if (transparent)
		{
			this.translucent.computeIfAbsent(stage, ignored -> new ObjectArrayList<>()).add(
					new TransparentSubmission(key, instance, distanceSquared(instance)));
			return;
		}
		this.opaque.computeIfAbsent(stage, ignored -> new Object2ObjectOpenHashMap<>()).
				computeIfAbsent(key, ignored -> new ObjectArrayList<>()).add(instance);
	}

	public PRenderPlan compile(S stage)
	{
		Map<DrawKey, List<PRenderQueue.InstanceData>> opaqueGroups = this.opaque.remove(stage);
		List<TransparentSubmission> translucentGroups = this.translucent.remove(stage);
		if (opaqueGroups == null && translucentGroups == null)
			return PRenderPlan.EMPTY;

		List<PDrawGroup> groups = new ObjectArrayList<>();
		if (opaqueGroups != null)
		{
			List<Map.Entry<DrawKey, List<PRenderQueue.InstanceData>>> entries = new ArrayList<>(opaqueGroups.entrySet());
			entries.sort(Comparator.comparingInt((Map.Entry<DrawKey, List<PRenderQueue.InstanceData>> entry) ->
					System.identityHashCode(entry.getKey().pipeline().outputTarget().getRenderTarget())).
					thenComparingInt(entry -> System.identityHashCode(entry.getKey().pipeline())));
			for (Map.Entry<DrawKey, List<PRenderQueue.InstanceData>> entry : entries)
				groups.add(group(entry.getKey(), true, entry.getValue()));
		}

		if (translucentGroups != null && !translucentGroups.isEmpty())
		{
			translucentGroups.sort(Comparator.comparingDouble(TransparentSubmission::distanceSquared).reversed());
			DrawKey activeKey = null;
			List<PRenderQueue.InstanceData> activeInstances = new ObjectArrayList<>();
			for (TransparentSubmission submission : translucentGroups)
			{
				if (activeKey != null && !activeKey.equals(submission.key()))
				{
					groups.add(group(activeKey, false, activeInstances));
					activeInstances = new ObjectArrayList<>();
				}
				activeKey = submission.key();
				activeInstances.add(submission.instance());
			}
			if (activeKey != null)
				groups.add(group(activeKey, false, activeInstances));
		}
		return groups.isEmpty() ? PRenderPlan.EMPTY : new PRenderPlan(groups);
	}

	public void clear()
	{
		this.opaque.clear();
		this.translucent.clear();
	}

	private static PDrawGroup group(DrawKey key, boolean writeDepth, List<PRenderQueue.InstanceData> instances)
	{
		return new PDrawGroup(key.pipeline(), key.mesh(), writeDepth, instances);
	}

	private static float distanceSquared(PRenderQueue.InstanceData instance)
	{
		return instance.posMatrix().m30() * instance.posMatrix().m30() +
				instance.posMatrix().m31() * instance.posMatrix().m31() +
				instance.posMatrix().m32() * instance.posMatrix().m32();
	}

	private record DrawKey(RenderType pipeline, PBakedMesh mesh) {}
	private record TransparentSubmission(DrawKey key, PRenderQueue.InstanceData instance, float distanceSquared) {}
}
