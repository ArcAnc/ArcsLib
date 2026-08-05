/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.model.animation;

<<<<<<< HEAD

import com.arcanc.pulselib.data.MolangParser;
=======
import com.arcanc.pulselib.content.registration.PLibRegistration;
import com.arcanc.pulselib.data.gecko.MolangParser;
import org.jetbrains.annotations.Nullable;
>>>>>>> e194067 (Tons of e)
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

public record PAnimation(String name,
                         float length,
                         Map<String, PBoneAnimation> boneAnimations,
                         List<PAnimationEvent> events)
{
	public PAnimation(String name, float length, Map<String, PBoneAnimation> boneAnimations)
	{
		this(name, length, boneAnimations, List.of());
	}
	
	public PAnimation
	{
		events = List.copyOf(events);
	}
	
	public PRootMotionRuntime rootMotion(String rootBoneName, PInterpolationType interpolation)
	{
<<<<<<< HEAD
		return calculateBoneTransformations(boneName, time, interpolationType, null, null);
=======
		return new PRootMotionRuntime(this, rootBoneName, interpolation);
	}
	
	public PRootMotionRuntime rootMotion(String rootBoneName, PInterpolationType interpolation, Object data)
	{
		return new PRootMotionRuntime(this, rootBoneName, interpolation, data);
	}

	public @Nullable BoneFrame calculateBoneTransformations(String boneName, float time, PInterpolationType interpolation)
	{
		return calculateBoneTransformations(boneName, time, interpolation, null, null);
	}

	public @Nullable BoneFrame calculateBoneTransformations(String boneName, float time, PInterpolationType interpolation, Object data)
	{
		return calculateBoneTransformations(boneName, time, interpolation, data, null);
>>>>>>> e194067 (Tons of e)
	}

	public @Nullable BoneFrame calculateBoneTransformations(String boneName,
	                                                        float time,
<<<<<<< HEAD
	                                                        PInterpolationType interpolationType,
=======
	                                                        PInterpolationType interpolation,
>>>>>>> e194067 (Tons of e)
	                                                        Object data,
	                                                        @Nullable BoneFrame accumulatedFrame)
	{
		PBoneAnimation boneAnimation = this.boneAnimations.get(boneName);
		return calculateBoneTransformations(boneAnimation, time, interpolation, data, accumulatedFrame);
	}

	public @Nullable BoneFrame calculateBoneTransformations(@Nullable PBoneAnimation boneAnimation,
	                                                        float time,
	                                                        PInterpolationType interpolation,
	                                                        Object data,
	                                                        @Nullable BoneFrame accumulatedFrame)
	{
		if (boneAnimation == null)
			return null;
<<<<<<< HEAD
		
		Vector3f accumulatedTranslation = accumulatedFrame == null ? new Vector3f() : accumulatedFrame.translation();
		Vector3f accumulatedScale = accumulatedFrame == null ? new Vector3f(1f, 1f, 1f) : accumulatedFrame.scale();
		Vector3f accumulatedRotation = accumulatedFrame == null ? new Vector3f() :
				accumulatedFrame.rotation().getEulerAnglesXYZ(new Vector3f()).mul((float)(180d / Math.PI));
		Vector3f translation = sampleVectorChannel(boneAnimation, PAnimationChannel.POSITION, time, interpolationType, data, accumulatedTranslation);
		
		Vector3f scale = sampleVectorChannel(boneAnimation, PAnimationChannel.SCALE, time, interpolationType, data, accumulatedScale);
		
		Quaternionf rotation = sampleRotationChannel(boneAnimation, time, interpolationType, data, accumulatedRotation);
		
		if (translation == null && rotation == null && scale == null)
=======

		PAnimationEvaluationContext context = new PAnimationEvaluationContext(
				data instanceof MolangParser.Context molang ? molang : new MolangParser.Context(), time);
		Vector3f accumulatedTranslation = accumulatedFrame == null ? new Vector3f() : accumulatedFrame.translation();
		Vector3f accumulatedScale = accumulatedFrame == null ? new Vector3f(1f) : accumulatedFrame.scale();
		Vector3f accumulatedRotation = accumulatedFrame == null ? new Vector3f() :
				accumulatedFrame.rotation().getEulerAnglesXYZ(new Vector3f()).mul((float) (180d / Math.PI));

		Vector3f translation = sample(boneAnimation, PLibRegistration.AnimationChannelReg.POSITION.get(), time, context, accumulatedTranslation);
		Vector3f scale = sample(boneAnimation, PLibRegistration.AnimationChannelReg.SCALE.get(), time, context, accumulatedScale);
		Quaternionf rotation = sample(boneAnimation, PLibRegistration.AnimationChannelReg.ROTATION.get(), time, context, accumulatedRotation);
		if (translation == null && scale == null && rotation == null)
>>>>>>> e194067 (Tons of e)
			return null;
		return new BoneFrame(
				translation == null ? new Vector3f() : translation,
				rotation == null ? new Quaternionf() : rotation,
				scale == null ? new Vector3f(1f) : scale);
	}

	public List<PAnimationEvent> eventsBetween(float from, float to)
	{
		if (this.events.isEmpty() || to < from)
			return List.of();
		return this.events.stream().filter(event -> (event.time() > from || (from == 0f && event.time() == 0f)) && event.time() <= to).toList();
	}
<<<<<<< HEAD
	
	@SuppressWarnings("unchecked")
	private @Nullable Vector3f sampleVectorChannel(PBoneAnimation boneAnimation, PAnimationChannel channel, float time, PInterpolationType interpolationType, Object data, Vector3f thisValue)
	{
		if (channel == PAnimationChannel.ROTATION)
			return null;
		setThisValue(data, thisValue);
		
		List<PKeyFrameChannel<Vector3f>> keyframes = (List<PKeyFrameChannel<Vector3f>>) boneAnimation.channels().get(channel);
		
		if (keyframes == null || keyframes.isEmpty())
=======

	private static <T> @Nullable T sample(PBoneAnimation boneAnimation,
	                                      PAnimationChannelType<T> channel,
	                                      float time,
	                                      PAnimationEvaluationContext context,
	                                      Vector3f thisValue)
	{
		PAnimationTrack<T> track = boneAnimation.track(channel);
		if (track == null || track.keyframes().isEmpty())
>>>>>>> e194067 (Tons of e)
			return null;
		context.thisValues(thisValue);
		List<PKeyframe<T>> keyframes = track.keyframes();
		if (keyframes.size() == 1)
			return evaluate(keyframes.getFirst().post(), channel, context);

		PKeyframe<T> previous = null;
		PKeyframe<T> next = null;
		for (PKeyframe<T> keyframe : keyframes)
		{
			if (keyframe.time() <= time)
				previous = keyframe;
			if (keyframe.time() > time)
			{
				next = keyframe;
				break;
			}
		}
		if (previous == null)
			return evaluate(keyframes.getFirst().pre(), channel, context);
		if (next == null)
<<<<<<< HEAD
			return new Vector3f(previous.value(data));
		
		float alpha = transformAlpha(previous, next, time, interpolationType);
		
		return new Vector3f(previous.value(data)).lerp(next.value(data), alpha);
	}
	
	@SuppressWarnings("unchecked")
	private @Nullable Quaternionf sampleRotationChannel(PBoneAnimation boneAnimation, float time, PInterpolationType interpolationType, Object data, Vector3f thisValue)
	{
		setThisValue(data, thisValue);
		List<PKeyFrameChannel<Quaternionf>> keyframes =
				(List<PKeyFrameChannel<Quaternionf>>) boneAnimation.channels().get(PAnimationChannel.ROTATION);
		
		if (keyframes == null || keyframes.isEmpty())
			return null;
		
		if (keyframes.size() == 1)
			return keyframes.getFirst().value(data);
		
		PKeyFrameChannel<Quaternionf> previous = null;
		PKeyFrameChannel<Quaternionf> next = null;
		
		for (PKeyFrameChannel<Quaternionf> kf : keyframes)
		{
			if (kf.time() <= time)
				previous = kf;
			
			if (kf.time() > time)
			{
				next = kf;
				break;
			}
		}
		
		if (previous == null)
			return keyframes.getFirst().value(data);
		
		if (next == null)
			return previous.value(data);
		
		float alpha = transformAlpha(previous, next, time, interpolationType);
		
		if (previous instanceof PKeyFrameChannel.RotationKeyFrame previousRotation &&
				next instanceof PKeyFrameChannel.RotationKeyFrame nextRotation)
		{
			Vector3f previousEuler = previousRotation.euler(data);
			Vector3f nextEuler = nextRotation.euler(data);
			if (previousEuler != null && nextEuler != null)
			{
				Vector3f euler = new Vector3f(previousEuler).lerp(nextEuler, alpha);
				return new Quaternionf().rotationXYZ(
						(float)Math.toRadians(euler.x),
						(float)Math.toRadians(euler.y),
						(float)Math.toRadians(euler.z));
			}
		}

		return new Quaternionf(previous.value(data)).slerp(next.value(data), alpha);
=======
			return evaluate(previous.post(), channel, context);

		T from = evaluate(previous.post(), channel, context);
		T to = evaluate(next.pre(), channel, context);
		T destination = channel.defaultValue();
		float alpha = (time - previous.time()) / (next.time() - previous.time());
		channel.interpolate(from, to, Math.clamp(alpha, 0f, 1f), previous.interpolation(), destination);
		return destination;
>>>>>>> e194067 (Tons of e)
	}

	private static <T> T evaluate(PAnimationValue<T> value, PAnimationChannelType<T> channel, PAnimationEvaluationContext context)
	{
<<<<<<< HEAD
		if (data instanceof MolangParser.Context context)
			context.thisValues(value.x(), value.y(), value.z());
	}

	private static <T> float transformAlpha(PKeyFrameChannel<T> previous,
	                                        PKeyFrameChannel<T> next,
	                                        float time,
	                                        PInterpolationType interpolationType)
	{
		float duration = next.time() - previous.time();
		if (duration <= 0.0f)
			return 0.0f;
		
		float rawAlpha = (time - previous.time()) / duration;
		rawAlpha = Math.clamp(rawAlpha, 0.0f, 1.0f);
		
		return (float) interpolationType.buildTransformer(rawAlpha);
=======
		T destination = channel.defaultValue();
		value.evaluate(context, destination);
		return destination;
>>>>>>> e194067 (Tons of e)
	}
}
