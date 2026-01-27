/**
 * @author ArcAnc
 * Created at: 26.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.content.model;


import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record ArcBone(
		UUID uuid,
		String name,
		int index,
		ArcBone parent,
		List<ArcBone> children,
		Vector3f pivot,
		Quaternionf baseRotation,
		Matrix4f bindPose,
		Matrix4f inverseBindPose,
		Matrix4f animatedPose
)
{
	public ArcBone(UUID uuid,
	               String name,
	               int index,
	               ArcBone parent,
	               Vector3f pivot,
	               Quaternionf baseRotation)
	{
		this(uuid, name, index, parent, new ArrayList<>(), pivot, baseRotation, new Matrix4f(), new Matrix4f(), new Matrix4f());
	}
	
}
