/**
 * @author ArcAnc
 * Created at: 24.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.content.renderer.base;


import com.arcanc.arcslib.content.animatable.ArcAnimatable;
import com.arcanc.arcslib.content.model.baked.ArcBakedModel;

public interface ArcRenderState<T extends ArcAnimatable<T>>
{
	void extractData();
	
	float getPartialTicks();
	
	ArcBakedModel getBakedModel();
	
	T getAnimatable();
}
