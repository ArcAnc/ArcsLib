/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.registration.item;


import com.arcanc.pulselib.content.animatable.PAnimationManager;
import com.arcanc.pulselib.content.animatable.PItemAnimatable;
import com.arcanc.pulselib.content.animatable.instance.PAnimationController;
import com.arcanc.pulselib.content.animatable.instance.ControllerState;
import com.arcanc.pulselib.content.model.animation.PRawAnimation;
import com.arcanc.pulselib.content.registration.item.renderer.TestBlockItemRenderer;
import com.arcanc.pulselib.content.renderer.modelData.DefaultItemModelData;
import com.arcanc.pulselib.util.Database;
import com.arcanc.pulselib.util.helpers.PLibHelper;
import com.arcanc.pulselib.util.helpers.RenderHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class TestBlockItem extends BlockItem implements PItemAnimatable<TestBlockItem>
{
	private final PAnimationManager<TestBlockItem> animationManager = PLibHelper.createManager(this);
	private final PRawAnimation idle = PRawAnimation.begin().
			thenLoop("idle").
			build();
	
	public TestBlockItem(Block block, Properties properties)
	{
		super(block, properties);
	}
	
	@Override
	public @NotNull PAnimationManager<TestBlockItem> getAnimationManager()
	{
		return this.animationManager;
	}
	
	@Override
	public void registerAnimationControllers(PAnimationManager.@NotNull PAnimationRegistrar<TestBlockItem> registrar)
	{
		registrar.add(new PAnimationController<>(state ->
		{
			state.controller().play(this.idle);
			return ControllerState.PLAY;
		}));
	}
	
	@Override
	public IClientItemExtensions registerClientExtension()
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
