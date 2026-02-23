/**
 * @author ArcAnc
 * Created at: 23.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.util.helpers;


import de.javagl.jgltf.model.AccessorData;
import de.javagl.jgltf.model.AccessorModel;
import de.javagl.jgltf.model.GltfConstants;
import org.jspecify.annotations.NonNull;

import java.nio.*;

public class ParserHelper
{
	public static @NonNull FloatBuffer getFloatBuffer(AccessorModel accessor)
	{
		if (accessor == null)
			return FloatBuffer.allocate(0);
		
		if (accessor.getComponentType() != GltfConstants.GL_FLOAT)
			throw new IllegalArgumentException("Accessor has wrong componentType. Type: " + accessor.getComponentType());
		
		AccessorData data = accessor.getAccessorData();
		ByteBuffer bb = data.createByteBuffer();
		bb.order(ByteOrder.LITTLE_ENDIAN);
		FloatBuffer floatBuffer = bb.asFloatBuffer();
		
		int componentCount = accessor.getElementType().getNumComponents();
		int totalFloats = accessor.getCount() * componentCount;
		
		floatBuffer.limit(totalFloats);
		
		return floatBuffer;
	}
	
	public static ByteBuffer getByteBuffer(AccessorModel accessor)
	{
		if (accessor == null)
			return null;
		
		AccessorData data = accessor.getAccessorData();
		
		ByteBuffer buffer = data.createByteBuffer();
		
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.rewind();
		
		int componentSize = switch (accessor.getComponentType())
		{
			case GltfConstants.GL_UNSIGNED_BYTE -> 1;
			case GltfConstants.GL_UNSIGNED_SHORT -> 2;
			case GltfConstants.GL_UNSIGNED_INT -> 4;
			default -> throw new IllegalStateException("Unsupported component type: " + accessor.getComponentType());
		};
		
		int componentCount =
				accessor.getElementType().getNumComponents();
		
		int expectedSize = accessor.getCount() * componentCount * componentSize;
		
		buffer.limit(expectedSize);
		
		return buffer;
	}
}
