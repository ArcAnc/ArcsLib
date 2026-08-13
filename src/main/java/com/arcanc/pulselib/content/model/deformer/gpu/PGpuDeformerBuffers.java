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

	public static void cleanup()
	{
		STREAM.clearDefinitions();
	}

	public record Submission(int operationOffset, int valueOffset, int operationCount)
	{
	}
}
