/**
 * @author ArcAnc
 * Created at: 15.03.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.renderer;


import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import com.arcanc.pulselib.content.model.deformer.gpu.PGpuDeformerBuffers;

public class PRenderStagesHandler
{
	public static void register(IEventBus modEventBus)
	{
		NeoForge.EVENT_BUS.addListener(PRenderStagesHandler :: renderLevelStages);
	}
	
	private static void renderLevelStages(final RenderLevelStageEvent event)
	{
		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES)
			PRenderQueue.flush(PRenderQueue.RenderStage.ENTITIES);
		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES)
			PRenderQueue.flush(PRenderQueue.RenderStage.SOLID_BLOCKS);
		else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES)
		{
			PRenderQueue.flush(PRenderQueue.RenderStage.TRANSLUCENT_BLOCKS);
			PGpuDeformerBuffers.finishFrame();
		}
		else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER)
		{
			RenderTarget destination = event.getLevelRenderer().getWeatherTarget();
			if (destination == null)
				destination = Minecraft.getInstance().getMainRenderTarget();
			PRenderQueue.compositeTranslucency(destination);
		}
	}
}
