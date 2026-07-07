/**
 * @author ArcAnc
 * Created at: 27.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.registration.entity.renderer;


import com.arcanc.pulselib.content.model.baked.PBakedBone;
import com.arcanc.pulselib.content.model.baked.PBakedMesh;
import com.arcanc.pulselib.content.model.baked.PMeshRenderContext;
import com.arcanc.pulselib.content.registration.entity.TestEntity;
import com.arcanc.pulselib.content.registration.renderer.TestDayTimeColor;
import com.arcanc.pulselib.content.renderer.PEntityRenderer;
import com.arcanc.pulselib.content.renderer.modelData.DefaultEntityModelData;
import com.arcanc.pulselib.util.PLibDatabase;
import com.arcanc.pulselib.util.PRenderTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class TestEntityRender extends PEntityRenderer<TestEntity>
{
	public static final ResourceLocation SPHERE = PLibDatabase.rl("entity/test_entity/sphere");
	public static final ResourceLocation TORUS = PLibDatabase.rl("entity/test_entity/torus");
	public static final ResourceLocation TUBE = PLibDatabase.rl("entity/test_entity/tube");
	public static final ResourceLocation ZERO = PLibDatabase.rl("entity/test_entity/0");
	public static final ResourceLocation ARMOR = PLibDatabase.rl("entity/test_entity/armor/0");
	
	public TestEntityRender(EntityRendererProvider.Context context)
	{
		super(context, new DefaultEntityModelData.DefaultEntityModelDataBuilder(PLibDatabase.rl("test_entity")).
				build(),
				PRenderTypes.RenderTypeProvider :: trianglesSolid);
		
		addRenderLayer("body", new PTestArmor().
				bindBone("armor_chest", "body").
				bindBone("armor_helm", "head").
				bindBone("armor_left_hand", "hand_left"));
	}
	
	@Override
	protected PMeshRenderContext resolveMeshRender(TestEntity animatable,
	                                               PBakedBone bone,
	                                               PBakedMesh mesh,
	                                               PMeshRenderContext inherited,
	                                               float partialTick)
	{
		return new PMeshRenderContext(
				inherited.renderType(),
				TestDayTimeColor.color(animatable.level(), partialTick),
				inherited.packedLight(),
				inherited.packedOverlay());
	}
}
