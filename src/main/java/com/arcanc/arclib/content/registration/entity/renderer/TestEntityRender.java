/**
 * @author ArcAnc
 * Created at: 27.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arclib.content.registration.entity.renderer;


import com.arcanc.arclib.content.registration.entity.TestEntity;
import com.arcanc.arclib.content.renderer.ArcEntityRenderer;
import com.arcanc.arclib.content.renderer.modelData.DefaultEntityModelData;
import com.arcanc.arclib.util.Database;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class TestEntityRender extends ArcEntityRenderer<TestEntity>
{
	public TestEntityRender(EntityRendererProvider.Context context)
	{
		super(context, new DefaultEntityModelData.DefaultEntityModelDataBuilder(Database.rl("test_entity")).
				addTexture(Database.rl("sphere")).
				addTexture(Database.rl("torus")).
				addTexture(Database.rl("tube")).
				build());
	}
	
	@Override
	public void preRender(PoseStack poseStack, TestEntity animatable, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float partialTick)
	{
	
	}
	
	@Override
	public void actuallyRender(PoseStack poseStack, TestEntity animatable, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float partialTick)
	{
	
	}
	
	@Override
	public void postRender(PoseStack poseStack, TestEntity animatable, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float partialTick)
	{
	
	}
}
