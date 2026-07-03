/**
 * @author ArcAnc
 * Created at: 03.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.util.armor;


import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

import java.util.function.Function;

public class PulseHumanoidAttachment extends PulseLivingAttachment
{
	public PulseHumanoidAttachment(PulseAttachmentAnchor anchor, String pulseBone)
	{
		super(anchor, pulseBone);
	}
	
	public PulseHumanoidAttachment(PulseAttachmentAnchor anchor, String pulseBone, Function<ResourceLocation, RenderType> renderType)
	{
		super(anchor, pulseBone, renderType);
	}
	
	public PulseHumanoidAttachment(PulseAttachmentAnchor anchor, String pulseBone, int color)
	{
		super(anchor, pulseBone, color);
	}
	
	public PulseHumanoidAttachment(PulseAttachmentAnchor anchor, String pulseBone, Function<ResourceLocation, RenderType> renderType, int color)
	{
		super(anchor, pulseBone, renderType, color);
	}
	
	public PulseHumanoidAttachment(PulseAttachmentAnchor anchor,
	                               String pulseBone,
	                               Function<ResourceLocation, RenderType> renderType,
	                               Vector3f offset,
	                               int color)
	{
		super(anchor, pulseBone, renderType, offset, color);
	}
	
	public PulseHumanoidAttachment(PulseAttachmentAnchor anchor,
	                               String pulseBone,
	                               Function<ResourceLocation, RenderType> renderType,
	                               Vector3f offset,
	                               Vector3f rotation,
	                               Vector3f scale,
	                               int color)
	{
		super(anchor, pulseBone, renderType, offset, rotation, scale, color);
	}
	
	protected PulseHumanoidAttachment(Builder<?> builder)
	{
		super(builder);
	}
	
	public static Builder<?> builder(PulseAttachmentAnchor anchor, String pulseBone)
	{
		return new Builder<>(anchor, pulseBone);
	}
	
	public static class Builder<T extends Builder<T>> extends PulseLivingAttachment.Builder<T>
	{
		protected Builder(PulseAttachmentAnchor anchor, String pulseBone)
		{
			super(anchor, pulseBone);
		}
		
		@Override
		public PulseHumanoidAttachment build()
		{
			return new PulseHumanoidAttachment(this);
		}
	}
}
