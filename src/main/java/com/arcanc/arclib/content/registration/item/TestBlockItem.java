/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arclib.content.registration.item;


import com.arcanc.arclib.content.animatable.ArcAnimatable;
import com.arcanc.arclib.content.animatable.ArcAnimationManager;
import com.arcanc.arclib.content.animatable.instance.ArcAnimationController;
import com.arcanc.arclib.content.animatable.instance.ControllerState;
import com.arcanc.arclib.content.model.animation.ArcRawAnimation;
import com.arcanc.arclib.content.registration.item.renderer.TestBlockItemRenderer;
import com.arcanc.arclib.content.renderer.modelData.DefaultItemModelData;
import com.arcanc.arclib.util.Database;
import com.arcanc.arclib.util.helpers.ArcLibHelper;
import com.arcanc.arclib.util.helpers.RenderHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

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
	public @NotNull ArcAnimationManager<TestBlockItem> getAnimationManager()
	{
		return this.animationManager;
	}
	
	@Override
	public void registerAnimationControllers(ArcAnimationManager.@NotNull ArcAnimationRegistrar<TestBlockItem> registrar)
	{
		registrar.add(new ArcAnimationController<>(state ->
		{
			state.controller().play(this.idle);
			return ControllerState.PLAY;
		}));
	}
	
	public IClientItemExtensions registerExtension()
	{
		return new IClientItemExtensions()
		{
			private final TestBlockItemRenderer renderer = new TestBlockItemRenderer
					(
							new DefaultItemModelData.DefaultItemModelDataBuilder(Database.rl("test_block")).
									addTexture(Database.rl("circle")).
									addTexture(Database.rl("pyramid")).
									build(),
							RenderHelper.mc().getBlockEntityRenderDispatcher(),
							RenderHelper.mc().getEntityModels()
					);
			@Override
			public @NotNull TestBlockItemRenderer getCustomRenderer()
			{
				return this.renderer;
			}
		};
	}
}
