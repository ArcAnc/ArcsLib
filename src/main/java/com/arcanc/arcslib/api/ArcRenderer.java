/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.api;


import com.arcanc.arcslib.content.model.ArcModel;
import net.minecraft.resources.Identifier;

public interface ArcRenderer<T extends ArcAnimatable>
{
	ArcModelData getArcModelData();
	
	default Identifier getTextureById(int id)
	{
		return getArcModelData().getTextureById(id);
	}
	
	ArcModel getArcModel();
	
	T getAnimatable();
}
