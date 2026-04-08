/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.registration.item;


import com.arcanc.pulselib.content.animatable.*;
import com.arcanc.pulselib.content.animatable.singleton.SingletonAnimationManager;
import com.arcanc.pulselib.content.model.animation.PRawAnimation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public class TestBlockItem extends BlockItem implements PAnimatable<TestBlockItem>
{
	private final PRawAnimation idle = PRawAnimation.begin().
			thenLoop("idle").
			build();
	
	public TestBlockItem(Block block, Properties properties)
	{
		super(block, properties);
	}
	
	@Override
	public PAnimationManager<TestBlockItem> getAnimationManager(AnimManagerKey key)
	{
		return SingletonAnimationManager.getManager(key, this);
	}
	
	@Override
	public void registerAnimationControllers(PAnimationManager.@NonNull PAnimationRegistrar<TestBlockItem> registrar)
	{
		registrar.add(() -> new PAnimationController<>(state ->
		{
			state.controller().play(this.idle);
			return ControllerState.PLAY;
		}));
	}
}
