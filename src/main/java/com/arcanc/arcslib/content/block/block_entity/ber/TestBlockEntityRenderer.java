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
import com.arcanc.arcslib.util.ArcRenderTypes;
import com.arcanc.arcslib.util.Database;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.jetbrains.annotations.NotNull;

public class TestBlockEntityRenderer extends ArcBlockRenderer<TestBlockEntity>
{
	public TestBlockEntityRenderer(final BlockEntityRendererProvider.Context ctx)
	{
		super(new ArcModelData(Database.rl("test_block"), "block",
				Database.rl("textures/block/test_block/tube_texture.png"),
				Database.rl("textures/block/test_block/torus_texture.png"),
				Database.rl("textures/block/test_block/pyramid_texture.png"),
				Database.rl("textures/block/test_block/cube_texture.png")));
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
		
		model.meshes.values().forEach(arcMesh ->
				submitNodeCollector.submitCustomGeometry(poseStack, ArcRenderTypes.trianglesSolid(getTextureById(arcMesh.texture())), (pose, vertexConsumer) ->
				{
					for (int q = 0; q < arcMesh.vertexCount(); q++)
					{
						vertexConsumer.addVertex(
								pose,
								arcMesh.positions().get(q * 3),
								arcMesh.positions().get(q * 3 + 1),
								arcMesh.positions().get(q * 3 + 2)).
						setColor(1f, 1f, 1f, 1f).
						setUv(
								arcMesh.uvs().get(q * 2),
								arcMesh.uvs().get(q * 2 + 1)).
						setNormal(
								pose,
								arcMesh.normals().get(q * 3),
								arcMesh.normals().get(q * 3 + 1),
								arcMesh.normals().get(q * 3 + 2)).
						setLight(blockEntityRenderState.lightCoords).
						setOverlay(OverlayTexture.NO_OVERLAY);
					}
				}));
		poseStack.popPose();
	}
}
