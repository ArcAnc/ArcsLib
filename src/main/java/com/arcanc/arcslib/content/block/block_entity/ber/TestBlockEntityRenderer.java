/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.content.block.block_entity.ber;


import com.arcanc.arcslib.api.ArcBlockRenderer;
import com.arcanc.arcslib.api.ArcModelData;
import com.arcanc.arcslib.content.block.block_entity.TestBlockEntity;
import com.arcanc.arcslib.content.model.ArcModel;
import com.arcanc.arcslib.util.Database;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import org.jetbrains.annotations.NotNull;

public class TestBlockEntityRenderer extends ArcBlockRenderer<TestBlockEntity>
{
	public TestBlockEntityRenderer(final BlockEntityRendererProvider.Context ctx)
	{
		super(new ArcModelData(Database.rl("test_block"), "block"));
	}
	
	@Override
	public void submit(BlockEntityRenderState blockEntityRenderState,
	                   @NotNull PoseStack poseStack,
	                   @NotNull SubmitNodeCollector submitNodeCollector,
	                   @NotNull CameraRenderState cameraRenderState)
	{
		ArcModel model = this.getArcModel();
		
		poseStack.pushPose();
		poseStack.translate(0.5f, 0.5f, 0.5f);
		
		/*submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.LINES, (pose, vertexConsumer) ->
		{
			vertexConsumer.addVertex(pose, 0, 0, 0).
					setColor(1f, 0f, 0f, 1f).
					setNormal(0, 0,0).
					setLineWidth(2.5f);
			
			vertexConsumer.addVertex(pose, 0, 0.5f, 0).
					setColor(1f, 0f, 0f, 1f).
					setNormal(0, 0,0).
					setLineWidth(2.5f);
		});*/
		poseStack.popPose();
	}
}
