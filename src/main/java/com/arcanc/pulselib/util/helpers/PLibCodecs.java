/**
 * @author ArcAnc
 * Created at: 05.04.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.util.helpers;


import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public class PLibCodecs
{
	public static final Codec<UUID> UUID_CODEC = UUIDUtil.CODEC;
	public static final MapCodec<UUID> UUID_MAP_CODEC = UUID_CODEC.fieldOf("uuid");
	public static final StreamCodec<ByteBuf, UUID> STREAM_CODEC = UUIDUtil.STREAM_CODEC;
}
