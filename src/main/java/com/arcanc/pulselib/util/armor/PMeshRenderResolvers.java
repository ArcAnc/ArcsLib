/**
 * @author ArcAnc
 * Created at: 07.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.util.armor;


public final class PMeshRenderResolvers
{
	private PMeshRenderResolvers()
	{
	}
	
	public static PMeshRenderResolver inherited()
	{
		return (entity, stack, bone, mesh, inherited, partialTick) -> inherited;
	}
	
	public static PMeshRenderResolver defaultLit()
	{
		return inherited();
	}
}
