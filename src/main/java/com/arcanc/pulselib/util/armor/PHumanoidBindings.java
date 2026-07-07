/**
 * @author ArcAnc
 * Created at: 07.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.util.armor;


import java.util.Objects;

public final class PHumanoidBindings
{
	private PHumanoidBindings()
	{
	}
	
	public static PAttachmentBinding bind(PulseAttachmentAnchor anchor, String bone)
	{
		return new Binding(anchor, bone, PTransform.IDENTITY);
	}
	
	public static PAttachmentBinding bind(PulseAttachmentAnchor anchor, String bone, PTransform transform)
	{
		return new Binding(anchor, bone, transform);
	}
	
	public static PAttachmentBinding head(String bone)
	{
		return bind(PulseHumanoidAnchors.HEAD, bone);
	}
	
	public static PAttachmentBinding body(String bone)
	{
		return bind(PulseHumanoidAnchors.BODY, bone);
	}
	
	public static PAttachmentBinding rightArm(String bone)
	{
		return bind(PulseHumanoidAnchors.RIGHT_ARM, bone);
	}
	
	public static PAttachmentBinding leftArm(String bone)
	{
		return bind(PulseHumanoidAnchors.LEFT_ARM, bone);
	}
	
	public static PAttachmentBinding rightLeg(String bone)
	{
		return bind(PulseHumanoidAnchors.RIGHT_LEG, bone);
	}
	
	public static PAttachmentBinding leftLeg(String bone)
	{
		return bind(PulseHumanoidAnchors.LEFT_LEG, bone);
	}
	
	private record Binding(PulseAttachmentAnchor anchor, String bone, PTransform transform) implements PAttachmentBinding
	{
		private Binding
		{
			Objects.requireNonNull(anchor);
			Objects.requireNonNull(bone);
			Objects.requireNonNull(transform);
		}
	}
}
