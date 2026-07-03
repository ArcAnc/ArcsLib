/**
 * @author ArcAnc
 * Created at: 04.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.registration.item;


import com.arcanc.pulselib.content.animatable.*;
import com.arcanc.pulselib.content.model.animation.PRawAnimation;
import com.arcanc.pulselib.content.model.baked.PBakedModel;
import com.arcanc.pulselib.content.renderer.modelData.PModelData;
import com.arcanc.pulselib.util.PLibDatabase;
import com.arcanc.pulselib.util.armor.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.*;

public class TestTailItem extends Item
{
	public static final ResourceLocation TEXTURE = PLibDatabase.rl("entity/attachment/test_tail/0");
	
	private static final PRawAnimation SWING = PRawAnimation.begin().
			thenLoop("swing").
			build();
	
	private static final PModelData MODEL_DATA = new PModelData.Builder(PLibDatabase.rl("attachment/test_tail"), "entity").build();
	private static final TailAnimatable ANIMATABLE = new TailAnimatable();
	private static final Map<UUID, TailController> CONTROLLERS = new HashMap<>();
	
	public TestTailItem(Properties properties)
	{
		super(properties);
		PulseLivingAttachments.register(this, createDefinition());
	}
	
	public static PulseLivingDefinition createDefinition(PulseLivingAttachmentSource source, PulseAttachmentAnchor anchor, Vector3f offset, Vector3f rotation)
	{
		return new PulseLivingDefinition(
				MODEL_DATA,
				source,
				List.of(PulseLivingAttachment.builder(anchor, "body").
						offset(offset).
						rotation(rotation).
						build()),
				false,
				TestTailItem :: controllers);
	}
	
	private PulseLivingDefinition createDefinition()
	{
		return createDefinition(PulseLivingAttachmentSource.hand(), PulseHumanoidAnchors.BODY, new Vector3f(), new Vector3f());
	}
	
	private static Collection<PAnimationController<?>> controllers(LivingEntity entity, ItemStack stack, PBakedModel model, float partialTick)
	{
		TailController tailController = CONTROLLERS.computeIfAbsent(entity.getUUID(), $ -> new TailController());
		if (tailController.lastTick != entity.tickCount)
		{
			tailController.controller.tick(ANIMATABLE, 1, model);
			tailController.lastTick = entity.tickCount;
		}
		
		return List.of(tailController.controller);
	}
	
	private static class TailController
	{
		private final PAnimationController<TailAnimatable> controller = new PAnimationController<>("tail", state ->
		{
			state.controller().play(SWING);
			return ControllerState.PLAY;
		});
		private int lastTick = -1;
	}
	
	private static class TailAnimatable implements PAnimatable<TailAnimatable>
	{
		@Override
		public PAnimationManager<TailAnimatable> getAnimationManager(AnimManagerKey key)
		{
			throw new UnsupportedOperationException("TestTailItem uses standalone attachment controllers");
		}
		
		@Override
		public void registerAnimationControllers(PAnimationManager.@NotNull PAnimationRegistrar<TailAnimatable> registrar)
		{
		}
	}
}
