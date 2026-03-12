/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.model.animation;


import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PBoneAnimation(UUID boneUuid, Map<PAnimationChannel, List<? extends PKeyFrameChannel<?>>> channels)
{
}
