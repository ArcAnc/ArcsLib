/**
 * @author ArcAnc
 * Created at: 28.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.model.baked;


import com.arcanc.pulselib.content.model.animation.PAnimation;

import java.util.List;
import java.util.Map;

public record PBakedModel(List<PBakedBone> bones, Map<String, PAnimation> animations)
{
}
