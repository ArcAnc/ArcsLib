/**
 * @author ArcAnc
 * Created at: 28.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.content.model.baked;


import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * WARNING! Name should be unique per bone!
 */
public record ArcBakedBone(String name,
                           Vector3f basePosition,
                           Quaternionf baseRotation,
                           List<ArcBakedBone> children,
                           ArcBakedBone parent,
                           List<ArcBakedMesh> meshes)
{
	public static final class ArcBakedBoneBuilder
	{
		public final UUID uuid;
		public final String name;
		public final Vector3f basePosition;
		public final Quaternionf baseRotation;
		
		public ArcBakedBoneBuilder parent;
		public final List<ArcBakedBoneBuilder> children = new ArrayList<>();
		public final List<ArcBakedMesh> meshes = new ArrayList<>();
		
		public ArcBakedBoneBuilder(
				UUID uuid,
				String name,
				Vector3f basePosition,
				Quaternionf baseRotation)
		{
			this.uuid = uuid;
			this.name = name;
			this.basePosition = basePosition;
			this.baseRotation = baseRotation;
		}
	}
}
