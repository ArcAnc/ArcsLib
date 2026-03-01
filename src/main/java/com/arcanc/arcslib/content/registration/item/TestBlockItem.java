/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.content.registration.item;


import com.arcanc.arcslib.content.animatable.ArcAnimatable;
import com.arcanc.arcslib.content.animatable.ArcAnimationManager;
import com.arcanc.arcslib.content.animatable.instance.ArcAnimationController;
import com.arcanc.arcslib.content.animatable.instance.ControllerState;
import com.arcanc.arcslib.content.model.animation.ArcRawAnimation;
import com.arcanc.arcslib.util.helpers.ArcLibHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

public class TestBlockItem extends BlockItem implements ArcAnimatable<TestBlockItem>
{
	private final ArcAnimationManager<TestBlockItem> animationManager = ArcLibHelper.createManager(this);
	private final ArcRawAnimation idle = ArcRawAnimation.begin().
			thenLoop("idle").
			build();
	
	public TestBlockItem(Block block, Properties properties)
	{
		super(block, properties);
	}
	
	@Override
	public ArcAnimationManager<TestBlockItem> getAnimationManager()
	{
		return this.animationManager;
	}
	
	@Override
	public void registerAnimationControllers(ArcAnimationManager.ArcAnimationRegistrar<TestBlockItem> registrar)
	{
		registrar.add(new ArcAnimationController<>(state ->
		{
			state.controller().play(this.idle);
			return ControllerState.PLAY;
		}));
	}
}
