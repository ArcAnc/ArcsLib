/**
 * @author ArcAnc
 * Created at: 05.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.registration.item;


import com.arcanc.pulselib.content.animatable.AnimManagerKey;
import com.arcanc.pulselib.content.animatable.ControllerState;
import com.arcanc.pulselib.content.animatable.PAnimatable;
import com.arcanc.pulselib.content.animatable.PAnimationController;
import com.arcanc.pulselib.content.animatable.PAnimationManager;
import com.arcanc.pulselib.content.model.animation.PRawAnimation;
import com.arcanc.pulselib.content.animatable.singleton.SingletonAnimationManager;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.NonNull;

public class TestArmorItem extends Item implements PAnimatable<TestArmorItem>
{
	private final PRawAnimation tailSwing = PRawAnimation.begin().
			thenLoop("swing").
			build();

	public TestArmorItem(Properties properties)
	{
		super(properties);
	}

	@Override
	public PAnimationManager<TestArmorItem> getAnimationManager(AnimManagerKey key)
	{
		return SingletonAnimationManager.getManager(key, this);
	}

	public PAnimationController<TestArmorItem> createTailController()
	{
		return new PAnimationController<>("tail", state ->
		{
			state.controller().play(this.tailSwing);
			return ControllerState.PLAY;
		});
	}

	@Override
	public void registerAnimationControllers(PAnimationManager.@NonNull PAnimationRegistrar<TestArmorItem> registrar)
	{
	}
}
