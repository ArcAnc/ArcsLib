/**
 * @author ArcAnc
 * Created at: 25.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.model.animation;


import org.joml.Quaternionf;
import org.joml.Vector3f;

public sealed interface PKeyFrameChannel<T> permits PKeyFrameChannel.PositionKeyFrame,
													PKeyFrameChannel.RotationKeyFrame,
													PKeyFrameChannel.ScaleKeyFrame
{
	float time();
	
	T value(Object data);

	default T value()
	{
		return value(null);
	}

	record PositionKeyFrame(float time, ValueProvider<Vector3f> valueProvider) implements PKeyFrameChannel<Vector3f>
	{
		public PositionKeyFrame(float time, Vector3f value)
		{
			this(time, data -> value);
		}

		@Override
		public Vector3f value(Object data)
		{
			return this.valueProvider.calculate(data);
		}
	}

	record RotationKeyFrame(float time, ValueProvider<Quaternionf> valueProvider) implements PKeyFrameChannel<Quaternionf>
	{
		public RotationKeyFrame(float time, Quaternionf value)
		{
			this(time, data -> value);
		}

		@Override
		public Quaternionf value(Object data)
		{
			return this.valueProvider.calculate(data);
		}
	}

	record ScaleKeyFrame(float time, ValueProvider<Vector3f> valueProvider) implements PKeyFrameChannel<Vector3f>
	{
		public ScaleKeyFrame(float time, Vector3f value)
		{
			this(time, data -> value);
		}

		@Override
		public Vector3f value(Object data)
		{
			return this.valueProvider.calculate(data);
		}
	}
	
	@FunctionalInterface
	interface ValueProvider<T>
	{
		T calculate(Object data);
	}
}
