/**
 * @author ArcAnc
 * Created at: 04.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.util.armor;


import net.minecraft.client.model.HumanoidModel;
import org.jetbrains.annotations.ApiStatus;

public class PulseHumanoidAnchors
{
	public static final PulseAttachmentAnchor HEAD = PulseAttachmentAnchor.minecraft("head");
	public static final PulseAttachmentAnchor HAT = PulseAttachmentAnchor.minecraft("hat");
	public static final PulseAttachmentAnchor BODY = PulseAttachmentAnchor.minecraft("body");
	public static final PulseAttachmentAnchor RIGHT_ARM = PulseAttachmentAnchor.minecraft("right_arm");
	public static final PulseAttachmentAnchor LEFT_ARM = PulseAttachmentAnchor.minecraft("left_arm");
	public static final PulseAttachmentAnchor RIGHT_LEG = PulseAttachmentAnchor.minecraft("right_leg");
	public static final PulseAttachmentAnchor LEFT_LEG = PulseAttachmentAnchor.minecraft("left_leg");
	
	@ApiStatus.Internal
	public static void registerDefaults()
	{
		PulseAttachmentAnchorResolvers.register(HumanoidModel.class, HEAD, (entity, model) -> ((HumanoidModel<?>)model).head);
		PulseAttachmentAnchorResolvers.register(HumanoidModel.class, HAT, (entity, model) -> ((HumanoidModel<?>)model).hat);
		PulseAttachmentAnchorResolvers.register(HumanoidModel.class, BODY, (entity, model) -> ((HumanoidModel<?>)model).body);
		PulseAttachmentAnchorResolvers.register(HumanoidModel.class, RIGHT_ARM, (entity, model) -> ((HumanoidModel<?>)model).rightArm);
		PulseAttachmentAnchorResolvers.register(HumanoidModel.class, LEFT_ARM, (entity, model) -> ((HumanoidModel<?>)model).leftArm);
		PulseAttachmentAnchorResolvers.register(HumanoidModel.class, RIGHT_LEG, (entity, model) -> ((HumanoidModel<?>)model).rightLeg);
		PulseAttachmentAnchorResolvers.register(HumanoidModel.class, LEFT_LEG, (entity, model) -> ((HumanoidModel<?>)model).leftLeg);
	}
}
