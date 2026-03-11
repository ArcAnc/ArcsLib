/**
 * @author ArcAnc
 * Created at: 11.03.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arclib.content.animatable;


import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public interface ArcItemAnimatable<T extends ArcAnimatable<T>> extends ArcAnimatable<T>
{
	IClientItemExtensions registerClientExtension();
}
