/**
 * @author ArcAnc
 * Created at: 03.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.util.armor;


import com.arcanc.pulselib.content.renderer.modelData.PModelData;

import java.util.List;

public class PulseHumanoidDefinition extends PulseLivingDefinition
{
	public PulseHumanoidDefinition(PModelData modelData, List<? extends PulseLivingAttachment> attachments)
	{
		super(modelData, attachments);
	}
	
	public PulseHumanoidDefinition(
			PModelData modelData,
			PulseLivingAttachmentSource source,
			List<? extends PulseLivingAttachment> attachments)
	{
		super(modelData, source, attachments);
	}
	
	public PulseHumanoidDefinition(
			PModelData modelData,
			PulseLivingAttachmentSource source,
			List<? extends PulseLivingAttachment> attachments,
			boolean hideVanilla)
	{
		super(modelData, source, attachments, hideVanilla);
	}
	
	public PulseHumanoidDefinition(
			PModelData modelData,
			PulseLivingAttachmentSource source,
			List<? extends PulseLivingAttachment> attachments,
			boolean hideVanilla,
			ControllerProvider controllerProvider)
	{
		super(modelData, source, attachments, hideVanilla, controllerProvider);
	}
}
