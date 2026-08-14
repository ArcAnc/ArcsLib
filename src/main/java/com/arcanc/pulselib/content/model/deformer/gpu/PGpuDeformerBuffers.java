/**
 * @author ArcAnc
 * Created at: 10.08.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.model.deformer.gpu;

import com.arcanc.pulselib.content.model.deformer.PMeshDeformation;
import com.arcanc.pulselib.util.PLibDatabase;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.renderer.MappableRingBuffer;
import org.jspecify.annotations.Nullable;

import java.util.List;

public final class PGpuDeformerBuffers
{
	public static final Submission NONE = new Submission(-1, -1, 0);
	private static final int MINIMUM_SIZE = Float.BYTES * 4;
	private static final PFrameStreams STREAMS = new PFrameStreams();
	private static @Nullable MappableRingBuffer operationsBuffer, valuesBuffer;

	private PGpuDeformerBuffers()
	{
	}

	public static Submission submit(@Nullable PMeshDeformation deformation)
	{
		return STREAMS.deformers().submit(deformation);
	}

	public static List<Float> operations()
	{
		return STREAMS.deformers().operations();
	}

	public static List<Float> values()
	{
		return STREAMS.deformers().values();
	}

	public static boolean operationsDirty()
	{
		return STREAMS.deformers().operationsDirty();
	}

	public static boolean valuesDirty()
	{
		return STREAMS.deformers().valuesDirty();
	}

	public static void markOperationsUploaded()
	{
		STREAMS.deformers().markOperationsUploaded();
	}

	public static void markValuesUploaded()
	{
		STREAMS.deformers().markValuesUploaded();
	}

	public static Bindings upload()
	{
		operationsBuffer = upload(operationsBuffer, operations(), operationsDirty(), "deformer_operations");
		valuesBuffer = upload(valuesBuffer, values(), valuesDirty(), "deformer_values");
		markOperationsUploaded();
		markValuesUploaded();
		return new Bindings(operationsBuffer.currentBuffer(), valuesBuffer.currentBuffer());
	}

	public static void finishFrame()
	{
		STREAMS.finishFrame();
	}

	public static void cleanup()
	{
		if (operationsBuffer != null)
			operationsBuffer.close();
		if (valuesBuffer != null)
			valuesBuffer.close();
		operationsBuffer = valuesBuffer = null;
		STREAMS.clearDefinitions();
	}

	public static void clearDefinitions()
	{
		STREAMS.clearDefinitions();
	}

	public static PFrameStreams streams()
	{
		return STREAMS;
	}

	private static MappableRingBuffer upload(@Nullable MappableRingBuffer buffer, List<Float> data, boolean dirty, String label)
	{
		int size = Math.max(MINIMUM_SIZE, data.size() * Float.BYTES);
		if (buffer == null || buffer.size() < size)
		{
			if (buffer != null)
				buffer.close();
			buffer = new MappableRingBuffer(() -> PLibDatabase.rl(label).toLanguageKey(),
					GpuBuffer.USAGE_UNIFORM_TEXEL_BUFFER | GpuBuffer.USAGE_MAP_WRITE, size);
			dirty = true;
		}
		else if (dirty)
			buffer.rotate();
		if (dirty)
			try (GpuBufferSlice.MappedView mapped = buffer.currentBuffer().map(false, true))
			{
				for (float value : data)
				{
					int bits = Float.floatToRawIntBits(value);
					mapped.data().put((byte)bits).put((byte)(bits >> 8)).put((byte)(bits >> 16)).put((byte)(bits >> 24));
				}
			}
		return buffer;
	}

	public record Bindings(GpuBuffer operations, GpuBuffer values) {}

	public record Submission(int operationOffset, int valueOffset, int operationCount)
	{
		public boolean enabled()
		{
			return this.operationCount > 0;
		}
	}
}
