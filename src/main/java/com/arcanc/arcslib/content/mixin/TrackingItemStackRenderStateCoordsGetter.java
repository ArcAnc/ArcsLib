/**
 * @author ArcAnc
 * Created at: 01.03.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.content.mixin;


import com.arcanc.arcslib.content.renderer.itemHacks.ITrackingItemStackRenderStateCoordsGetter;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin (TrackingItemStackRenderState.class)
public class TrackingItemStackRenderStateCoordsGetter implements ITrackingItemStackRenderStateCoordsGetter
{
	@Unique
	private int arcsLib$x;
	@Unique
	private int arcsLib$y;
	
	@Unique
	@Override
	public int arcsLib$getX()
	{
		return this.arcsLib$x;
	}
	
	@Unique
	@Override
	public int arcsLib$getY()
	{
		return this.arcsLib$y;
	}
	
	@Override
	public void arcsLib$setX(int x)
	{
		this.arcsLib$x = x;
	}
	
	@Override
	public void arcsLib$setY(int y)
	{
		this.arcsLib$y = y;
	}
}
