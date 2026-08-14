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
import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.List;

public record PDrawGroup(
		RenderType pipeline,
		PBakedMesh mesh,
		boolean writeDepth,
		List<PRenderQueue.InstanceData> instances)
{
	public PDrawGroup
	{
		instances = List.copyOf(instances);
	}
}
