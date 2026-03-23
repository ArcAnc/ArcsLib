/**
 * @author ArcAnc
 * Created at: 27.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.registration.entity.renderer;


import com.arcanc.pulselib.content.registration.entity.TestEntity;
import com.arcanc.pulselib.content.renderer.PEntityRenderer;
import com.arcanc.pulselib.content.renderer.modelData.DefaultEntityModelData;
import com.arcanc.pulselib.util.PLibDatabase;
import com.arcanc.pulselib.util.PRenderTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class TestEntityRender extends PEntityRenderer<TestEntity>
{
	public TestEntityRender(EntityRendererProvider.Context context)
	{
		super(context, new DefaultEntityModelData.DefaultEntityModelDataBuilder(PLibDatabase.rl("test_entity")).
				addTexture(PLibDatabase.rl("sphere")).
				addTexture(PLibDatabase.rl("torus")).
				addTexture(PLibDatabase.rl("tube")).
				build(),
				PRenderTypes.RenderTypeProvider :: trianglesSolid);
	}
}
