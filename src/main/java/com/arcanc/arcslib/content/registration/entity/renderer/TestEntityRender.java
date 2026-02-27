/**
 * @author ArcAnc
 * Created at: 27.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.content.registration.entity.renderer;


import com.arcanc.arcslib.content.registration.entity.TestEntity;
import com.arcanc.arcslib.content.registration.entity.renderer.renderState.TestEntityRenderState;
import com.arcanc.arcslib.content.renderer.ArcEntityRenderer;
import com.arcanc.arcslib.content.renderer.ArcModelData;
import com.arcanc.arcslib.util.Database;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class TestEntityRender extends ArcEntityRenderer<TestEntity, TestEntityRenderState>
{
	public TestEntityRender(EntityRendererProvider.Context context)
	{
		super(context, new ArcModelData(Database.rl("test_entity"), "entity",
				Database.rl("textures/entity/test_entity/sphere.png"),
				Database.rl("textures/entity/test_entity/torus.png"),
				Database.rl("textures/entity/test_entity/tube.png")));
	}
	
	@Override
	public TestEntityRenderState createRenderState()
	{
		return new TestEntityRenderState();
	}
}
