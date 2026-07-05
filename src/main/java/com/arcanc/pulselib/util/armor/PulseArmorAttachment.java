/**
 * @author ArcAnc
 * Created at: 05.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.util.armor;


import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.function.Function;

public class PulseArmorAttachment extends PulseHumanoidAttachment
{
	private final EquipmentSlot slot;

	public PulseArmorAttachment(
			EquipmentSlot slot,
			PulseAttachmentAnchor anchor,
			String pulseBone)
	{
		super(anchor, pulseBone);
		this.slot = Objects.requireNonNull(slot);
	}

	public PulseArmorAttachment(
			EquipmentSlot slot,
			PulseAttachmentAnchor anchor,
			String pulseBone,
			int color)
	{
		super(anchor, pulseBone, color);
		this.slot = Objects.requireNonNull(slot);
	}

	public PulseArmorAttachment(
			EquipmentSlot slot,
			PulseAttachmentAnchor anchor,
			String pulseBone,
			Function<Identifier, RenderType> renderType,
			Vector3f offset,
			int color)
	{
		super(anchor, pulseBone, renderType, offset, color);
		this.slot = Objects.requireNonNull(slot);
	}

	public PulseArmorAttachment(
			EquipmentSlot slot,
			PulseAttachmentAnchor anchor,
			String pulseBone,
			Function<Identifier, RenderType> renderType,
			Vector3f offset,
			Vector3f rotation,
			Vector3f scale,
			int color)
	{
		super(anchor, pulseBone, renderType, offset, rotation, scale, color);
		this.slot = Objects.requireNonNull(slot);
	}

	public PulseArmorAttachment(
			EquipmentSlot slot,
			PulseAttachmentAnchor anchor,
			String pulseBone,
			Function<Identifier, RenderType> renderType,
			int color)
	{
		this(slot, anchor, pulseBone, renderType, new Vector3f(), color);
	}

	public PulseArmorAttachment(
			EquipmentSlot slot,
			PulseAttachmentAnchor anchor,
			String pulseBone,
			Function<Identifier, RenderType> renderType)
	{
		this(slot, anchor, pulseBone, renderType, new Vector3f(), -1);
	}

	public EquipmentSlot slot()
	{
		return this.slot;
	}

	protected PulseArmorAttachment(Builder builder)
	{
		super(builder);
		this.slot = builder.slot;
	}

	public static Builder builder(EquipmentSlot slot, PulseAttachmentAnchor anchor, String pulseBone)
	{
		return new Builder(slot, anchor, pulseBone);
	}

	public static class Builder extends PulseHumanoidAttachment.Builder<Builder>
	{
		protected final EquipmentSlot slot;

		protected Builder(EquipmentSlot slot, PulseAttachmentAnchor anchor, String pulseBone)
		{
			super(anchor, pulseBone);
			this.slot = Objects.requireNonNull(slot);
		}

		@Override
		public PulseArmorAttachment build()
		{
			return new PulseArmorAttachment(this);
		}
	}
}
