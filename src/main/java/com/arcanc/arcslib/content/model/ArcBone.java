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
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ArcBone
{
	private final UUID uuid;
	private final String name;
	private final List<UUID> meshUUIDS;
	private ArcBone parent;
	private final List<ArcBone> children;
	private final Vector3f pivot;
	private final Quaternionf baseRotation;
	private final Matrix4f bindPose;
	private final Matrix4f inverseBindPose;
	private final Matrix4f animatedPose;
	
	
	public ArcBone(
			UUID uuid,
			String name,
			List<UUID> meshUUIDS,
			ArcBone parent,
			List<ArcBone> children,
			Vector3f pivot,
			Quaternionf baseRotation,
			Matrix4f bindPose,
			Matrix4f inverseBindPose,
			Matrix4f animatedPose)
	{
		this.uuid = uuid;
		this.name = name;
		this.meshUUIDS = meshUUIDS;
		this.parent = parent;
		this.children = children;
		this.pivot = pivot;
		this.baseRotation = baseRotation;
		this.bindPose = bindPose;
		this.inverseBindPose = inverseBindPose;
		this.animatedPose = animatedPose;
	}
	
	public ArcBone(UUID uuid, String name, Vector3f pivot, Quaternionf baseRotation)
	{
		this(uuid, name, new ArrayList<>(), null, new ArrayList<>(), pivot, baseRotation, new Matrix4f(), new Matrix4f(), new Matrix4f());
	}
	
	public UUID uuid()
	{
		return this.uuid;
	}
	
	public String name()
	{
		return this.name;
	}
	
	public List<UUID> meshUUIDS()
	{
		return this.meshUUIDS;
	}
	
	public @Nullable ArcBone parent()
	{
		return this.parent;
	}
	
	public void setParent(ArcBone parent)
	{
		this.parent = parent;
	}
	
	public List<ArcBone> children()
	{
		return this.children;
	}
	
	public Vector3f pivot()
	{
		return this.pivot;
	}
	
	public Quaternionf baseRotation()
	{
		return this.baseRotation;
	}
	
	public Matrix4f bindPose()
	{
		return this.bindPose;
	}
	
	public Matrix4f inverseBindPose()
	{
		return this.inverseBindPose;
	}
	
	public Matrix4f animatedPose()
	{
		return this.animatedPose;
	}
}

