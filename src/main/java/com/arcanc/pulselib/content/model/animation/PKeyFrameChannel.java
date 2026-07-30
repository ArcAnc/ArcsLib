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

	final class RotationKeyFrame implements PKeyFrameChannel<Quaternionf>
	{
		private final float time;
		private final ValueProvider<Quaternionf> valueProvider;
		private final ValueProvider<Vector3f> eulerValueProvider;

		public RotationKeyFrame(float time, Quaternionf value)
		{
			this(time, data -> value);
		}

		public RotationKeyFrame(float time, ValueProvider<Quaternionf> valueProvider)
		{
			this(time, valueProvider, null);
		}

		private RotationKeyFrame(float time,
		                         ValueProvider<Quaternionf> valueProvider,
		                         ValueProvider<Vector3f> eulerValueProvider)
		{
			this.time = time;
			this.valueProvider = valueProvider;
			this.eulerValueProvider = eulerValueProvider;
		}

		public RotationKeyFrame(float time, Vector3f eulerDegrees)
		{
			this(time,
					data -> new Quaternionf().rotationXYZ(
							(float)Math.toRadians(eulerDegrees.x),
							(float)Math.toRadians(eulerDegrees.y),
							(float)Math.toRadians(eulerDegrees.z)),
					data -> eulerDegrees);
		}

		public static RotationKeyFrame euler(float time, ValueProvider<Vector3f> eulerValueProvider)
		{
			return new RotationKeyFrame(time,
					data ->
					{
						Vector3f euler = eulerValueProvider.calculate(data);
						return new Quaternionf().rotationXYZ(
								(float)Math.toRadians(euler.x),
								(float)Math.toRadians(euler.y),
								(float)Math.toRadians(euler.z));
					},
					eulerValueProvider);
		}

		@Override
		public float time()
		{
			return this.time;
		}

		@Override
		public Quaternionf value(Object data)
		{
			return this.valueProvider.calculate(data);
		}

		public Vector3f euler(Object data)
		{
			return this.eulerValueProvider == null ? null : this.eulerValueProvider.calculate(data);
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
