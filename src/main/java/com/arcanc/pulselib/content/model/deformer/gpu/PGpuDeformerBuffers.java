/**
 * @author ArcAnc
 * Created at: 10.08.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.model.deformer.gpu;

import com.arcanc.pulselib.content.model.deformer.PChannelReference;
import com.arcanc.pulselib.content.model.deformer.PDeformerStack;
import com.arcanc.pulselib.content.model.deformer.PMeshDeformation;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class PGpuDeformerBuffers
{
	public static final Submission NONE = new Submission(-1, -1, 0);

	private static final Map<PDeformerStack, @Nullable PGpuDeformerStack> STACKS = new IdentityHashMap<>();
	private static final Map<PGpuDeformerStack, Integer> OPERATION_OFFSETS = new IdentityHashMap<>();
	private static final List<Float> OPERATIONS = new ArrayList<>();
	private static final List<Float> VALUES = new ArrayList<>();

	private static int operationsBuffer = -1;
	private static int operationsTexture = -1;
	private static int valuesBuffer = -1;
	private static int valuesTexture = -1;
	private static boolean operationsDirty;
	private static boolean valuesDirty;

	private PGpuDeformerBuffers()
	{
	}
	
	public static Submission submit(@Nullable PMeshDeformation deformation)
	{
		if (deformation == null || deformation.stack().isEmpty())
			return NONE;
		PGpuDeformerStack stack = resolve(deformation.stack());
		if (stack == null)
			return NONE;

		int operationOffset = OPERATION_OFFSETS.computeIfAbsent(stack, PGpuDeformerBuffers :: appendOperations);
		int valueOffset = VALUES.size() / 4;
		for (PChannelReference<Float> reference : stack.channels())
		{
			VALUES.add(deformation.values().resolve(reference));
			VALUES.add(0.0f);
			VALUES.add(0.0f);
			VALUES.add(0.0f);
		}
		valuesDirty = true;
		return new Submission(operationOffset, valueOffset, stack.operationCount());
	}

	private static @Nullable PGpuDeformerStack resolve(PDeformerStack stack)
	{
		if (STACKS.containsKey(stack))
			return STACKS.get(stack);
		PGpuDeformerStack resolved = PGpuDeformerStack.compile(stack).orElse(null);
		STACKS.put(stack, resolved);
		return resolved;
	}

	private static int appendOperations(PGpuDeformerStack stack)
	{
		int result = OPERATIONS.size() / 4;
		for (float value : stack.operationData())
			OPERATIONS.add(value);
		operationsDirty = true;
		return result;
	}
	
	public static void upload()
	{
		ensureTextures();
		if (operationsDirty)
		{
			upload(operationsBuffer, OPERATIONS, GL15.GL_STATIC_DRAW);
			operationsDirty = false;
		}
		if (valuesDirty)
		{
			upload(valuesBuffer, VALUES, GL15.GL_STREAM_DRAW);
			valuesDirty = false;
		}
	}

	public static int operationsTexture()
	{
		ensureTextures();
		return operationsTexture;
	}

	public static int valuesTexture()
	{
		ensureTextures();
		return valuesTexture;
	}
	
	public static void bind(ShaderInstance shader)
	{
		upload();
		int activeTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
		int program = shader.getId();
		bindSampler(program, "DeformerOperations", 7, operationsTexture);
		bindSampler(program, "DeformerValues", 8, valuesTexture);
		GL13.glActiveTexture(activeTexture);
	}

	private static void bindSampler(int program, String name, int unit, int texture)
	{
		int location = GL20.glGetUniformLocation(program, name);
		if (location < 0)
			return;
		GL20.glUniform1i(location, unit);
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
		GL11.glBindTexture(GL31.GL_TEXTURE_BUFFER, texture);
	}
	
	public static void finishFrame()
	{
		VALUES.clear();
		valuesDirty = true;
	}

	public static void cleanup()
	{
		if (operationsTexture != -1)
			GL11.glDeleteTextures(operationsTexture);
		if (valuesTexture != -1)
			GL11.glDeleteTextures(valuesTexture);
		if (operationsBuffer != -1)
			GL15.glDeleteBuffers(operationsBuffer);
		if (valuesBuffer != -1)
			GL15.glDeleteBuffers(valuesBuffer);
		operationsTexture = valuesTexture = operationsBuffer = valuesBuffer = -1;
		STACKS.clear();
		OPERATION_OFFSETS.clear();
		OPERATIONS.clear();
		VALUES.clear();
		operationsDirty = valuesDirty = false;
	}
	
	public static void clearDefinitions()
	{
		STACKS.clear();
		OPERATION_OFFSETS.clear();
		OPERATIONS.clear();
		VALUES.clear();
		operationsDirty = true;
		valuesDirty = true;
	}

	private static void ensureTextures()
	{
		if (operationsBuffer != -1)
			return;
		operationsBuffer = GL15.glGenBuffers();
		operationsTexture = GL11.glGenTextures();
		valuesBuffer = GL15.glGenBuffers();
		valuesTexture = GL11.glGenTextures();
		GL15.glBindBuffer(GL31.GL_TEXTURE_BUFFER, operationsBuffer);
		GL15.glBufferData(GL31.GL_TEXTURE_BUFFER, 16L, GL15.GL_STATIC_DRAW);
		GL11.glBindTexture(GL31.GL_TEXTURE_BUFFER, operationsTexture);
		GL31.glTexBuffer(GL31.GL_TEXTURE_BUFFER, GL30.GL_RGBA32F, operationsBuffer);
		GL15.glBindBuffer(GL31.GL_TEXTURE_BUFFER, valuesBuffer);
		GL15.glBufferData(GL31.GL_TEXTURE_BUFFER, 16L, GL15.GL_STREAM_DRAW);
		GL11.glBindTexture(GL31.GL_TEXTURE_BUFFER, valuesTexture);
		GL31.glTexBuffer(GL31.GL_TEXTURE_BUFFER, GL30.GL_RGBA32F, valuesBuffer);
		GL11.glBindTexture(GL31.GL_TEXTURE_BUFFER, 0);
		GL15.glBindBuffer(GL31.GL_TEXTURE_BUFFER, 0);
	}

	private static void upload(int buffer, List<Float> values, int usage)
	{
		int vec4Count = Math.max(1, (values.size() + 3) / 4);
		FloatBuffer packed = BufferUtils.createFloatBuffer(vec4Count * 4);
		for (float value : values)
			packed.put(value);
		packed.flip();
		GL15.glBindBuffer(GL31.GL_TEXTURE_BUFFER, buffer);
		GL15.glBufferData(GL31.GL_TEXTURE_BUFFER, packed, usage);
		GL15.glBindBuffer(GL31.GL_TEXTURE_BUFFER, 0);
	}

	public record Submission(int operationOffset, int valueOffset, int operationCount)
	{
		public boolean enabled()
		{
			return this.operationCount > 0;
		}
	}
}
