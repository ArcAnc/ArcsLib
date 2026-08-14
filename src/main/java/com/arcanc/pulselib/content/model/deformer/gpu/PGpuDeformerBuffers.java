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
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.MappableRingBuffer;
import org.jspecify.annotations.Nullable;

import java.util.List;

public final class PGpuDeformerBuffers
{
	public static final Submission NONE = new Submission(-1, -1, 0);
	private static final PDeformerStream STREAM = new PDeformerStream();

	private PGpuDeformerBuffers()
	{
	}

	public static Submission submit(@Nullable PMeshDeformation deformation)
	{
		return STREAM.submit(deformation);
	}

	public static List<Float> operations()
	{
		return STREAM.operations();
	}

	public static List<Float> values()
	{
		return STREAM.values();
	}

	public static boolean operationsDirty()
	{
		return STREAM.operationsDirty();
	}

	public static boolean valuesDirty()
	{
		return STREAM.valuesDirty();
	}

	public static void markOperationsUploaded()
	{
		STREAM.markOperationsUploaded();
	}

	public static void markValuesUploaded()
	{
		STREAM.markValuesUploaded();
	}

	public static void finishFrame()
	{
		STREAM.finishFrame();
	}

	public static Bindings upload()
	{
		return UPLOAD_BUFFERS.upload();
	}

	public static void cleanup()
	{
		UPLOAD_BUFFERS.close();
		STREAM.clearDefinitions();
	}

	public record Submission(int operationOffset, int valueOffset, int operationCount)
	{
	}

	public record Bindings(GpuBuffer operations, GpuBuffer values)
	{
	}

	private static final UploadBuffers UPLOAD_BUFFERS = new UploadBuffers();

	private static final class UploadBuffers
	{
		private static final int MINIMUM_SIZE = Float.BYTES * 4;

		private @Nullable MappableRingBuffer operations;
		private @Nullable MappableRingBuffer values;

		private Bindings upload()
		{
			this.operations = upload(this.operations, PGpuDeformerBuffers.operations(), PGpuDeformerBuffers.operationsDirty(), "deformer_operations");
			this.values = upload(this.values, PGpuDeformerBuffers.values(), PGpuDeformerBuffers.valuesDirty(), "deformer_values");
			PGpuDeformerBuffers.markOperationsUploaded();
			PGpuDeformerBuffers.markValuesUploaded();
			return new Bindings(this.operations.currentBuffer(), this.values.currentBuffer());
		}

		private void close()
		{
			if (this.operations != null)
			{
				this.operations.close();
				this.operations = null;
			}
			if (this.values != null)
			{
				this.values.close();
				this.values = null;
			}
		}

		private static MappableRingBuffer upload(@Nullable MappableRingBuffer buffer,
		                                         List<Float> data,
		                                         boolean dirty,
		                                         String label)
		{
			int size = Math.max(MINIMUM_SIZE, data.size() * Float.BYTES);
			if (buffer == null || buffer.size() < size)
			{
				if (buffer != null)
					buffer.close();
				buffer = new MappableRingBuffer(
						() -> PLibDatabase.rl(label).toLanguageKey(),
						GpuBuffer.USAGE_UNIFORM_TEXEL_BUFFER | GpuBuffer.USAGE_MAP_WRITE,
						size);
				dirty = true;
			}
			else if (dirty)
				buffer.rotate();
			if (dirty)
				try (GpuBuffer.MappedView mapped = RenderSystem.getDevice().createCommandEncoder().mapBuffer(buffer.currentBuffer(), false, true))
				{
					var bytes = mapped.data();
					for (float value : data)
					{
						int bits = Float.floatToRawIntBits(value);
						bytes.put((byte)bits);
						bytes.put((byte)(bits >> 8));
						bytes.put((byte)(bits >> 16));
						bytes.put((byte)(bits >> 24));
					}
				}
			return buffer;
		}
	}
}
