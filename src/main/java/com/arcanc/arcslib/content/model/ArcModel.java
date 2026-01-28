/**
 * @author ArcAnc
 * Created at: 26.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.content.model;


import com.arcanc.arcslib.content.model.animation.ArcAnimation;
import com.mojang.datafixers.util.Pair;

import java.util.*;

public class ArcModel
{
	public final Map<UUID, ArcBone> bones = new LinkedHashMap<>();
	public final Map<UUID, ArcMesh> meshes = new LinkedHashMap<>();
	public final Map<UUID, Pair<UUID, List<UUID>>> boneMeshes = new HashMap<>();
	public final Map<String, ArcAnimation> animations = new HashMap<>();
}
