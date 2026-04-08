/**
 * @author ArcAnc
 * Created at: 24.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.renderer.base;


import com.arcanc.pulselib.content.animatable.AnimManagerKey;
import com.arcanc.pulselib.content.animatable.PAnimatable;
import com.arcanc.pulselib.content.model.baked.PBakedModel;
import org.jspecify.annotations.Nullable;

public interface PRenderState<T extends PAnimatable<T>>
{
	void extractData();
	
	float partialTick();
	
	@Nullable PBakedModel getBakedModel();
	
	T getAnimatable();
	
	AnimManagerKey getAnimKey();
}
