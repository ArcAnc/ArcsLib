/**
 * @author ArcAnc
 * Created at: 05.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.util.armor;


import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.neoforged.bus.api.IEventBus;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public class PulseAttachmentAnchorResolvers
{
	private static final Map<Class<?>, Map<PulseAttachmentAnchor, AnchorResolver>> RESOLVERS = new Object2ObjectLinkedOpenHashMap<>();

	@ApiStatus.Internal
	public static void init(IEventBus bus)
	{
		PulseHumanoidAnchors.registerDefaults();
	}

	public static void register(
			Class<?> modelClass,
			PulseAttachmentAnchor anchor,
			AnchorResolver resolver)
	{
		RESOLVERS.computeIfAbsent(modelClass, _ -> new Object2ObjectLinkedOpenHashMap<>()).put(anchor, resolver);
	}

	public static @Nullable <S extends EntityRenderState> ModelPart resolve(S state, EntityModel<? super S> model, PulseAttachmentAnchor anchor)
	{
		for (Map.Entry<Class<?>, Map<PulseAttachmentAnchor, AnchorResolver>> entry : RESOLVERS.entrySet())
		{
			if (!entry.getKey().isInstance(model))
				continue;

			AnchorResolver resolver = entry.getValue().get(anchor);
			if (resolver == null)
				continue;

			return resolver.resolve(state, model);
		}

		return null;
	}

	@FunctionalInterface
	public interface AnchorResolver
	{
		@Nullable ModelPart resolve(EntityRenderState state, EntityModel<?> model);
	}
}
