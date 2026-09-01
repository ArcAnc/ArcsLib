/**
 * @author ArcAnc
 * Created at: 01.09.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.registration;


import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Small client-side registry for PulseLib extension types.
 */
public final class PRegistry<T>
{
	private final Map<ResourceLocation, T> values = new Object2ObjectOpenHashMap<>();

	public <V extends T> V register(ResourceLocation id, V value)
	{
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(value, "value");
		if (this.values.putIfAbsent(id, value) != null)
			throw new IllegalArgumentException("Duplicate PulseLib registry entry " + id);
		return value;
	}

	public Optional<T> get(ResourceLocation id)
	{
		return Optional.ofNullable(this.values.get(id));
	}
}
