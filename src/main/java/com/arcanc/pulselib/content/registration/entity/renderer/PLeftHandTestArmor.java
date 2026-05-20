/**
 * @author ArcAnc
 * Created at: 20.05.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.registration.entity.renderer;


import com.arcanc.pulselib.content.registration.Registration;
import com.arcanc.pulselib.content.registration.entity.TestEntity;
import com.arcanc.pulselib.content.renderer.PEntityRenderLayer;
import com.arcanc.pulselib.content.renderer.base.PEntityRenderState;
import com.arcanc.pulselib.content.renderer.modelData.DefaultEntityLayerModelData;
import com.arcanc.pulselib.util.PLibDatabase;
import com.arcanc.pulselib.util.PRenderTypes;

public class PLeftHandTestArmor extends PEntityRenderLayer<TestEntity, PEntityRenderState.LivingImpl<TestEntity>>
{
	public PLeftHandTestArmor()
	{
		super(new DefaultEntityLayerModelData.
						DefaultEntityLayerModelDataBuilder(Registration.EntityTypeReg.TEST_ENTITY.getId(), PLibDatabase.rl("left_hand")).
						build(),
				PRenderTypes.RenderTypeProvider :: trianglesSolid);
	}
	
	@Override
	public boolean shouldRender(PEntityRenderState.LivingImpl<TestEntity> renderState)
	{
		return renderState.getAnimatable().showArmor;
	}
}
