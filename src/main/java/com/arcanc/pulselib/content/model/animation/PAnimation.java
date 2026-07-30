/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.model.animation;


import com.arcanc.pulselib.data.MolangParser;
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
	
	public @Nullable BoneFrame calculateBoneTransformations(String boneName, float time, PInterpolationType interpolationType)
	{
		return calculateBoneTransformations(boneName, time, interpolationType, null, null);
	}

	public @Nullable BoneFrame calculateBoneTransformations(String boneName,
	                                                        float time,
	                                                        PInterpolationType interpolationType,
	                                                        Object data,
	                                                        @Nullable BoneFrame accumulatedFrame)
	{
		PBoneAnimation boneAnimation = this.boneAnimations.get(boneName);
		if (boneAnimation == null)
			return null;
		
		Vector3f accumulatedTranslation = accumulatedFrame == null ? new Vector3f() : accumulatedFrame.translation();
		Vector3f accumulatedScale = accumulatedFrame == null ? new Vector3f(1f, 1f, 1f) : accumulatedFrame.scale();
		Vector3f accumulatedRotation = accumulatedFrame == null ? new Vector3f() :
				accumulatedFrame.rotation().getEulerAnglesXYZ(new Vector3f()).mul((float)(180d / Math.PI));
		Vector3f translation = sampleVectorChannel(boneAnimation, PAnimationChannel.POSITION, time, interpolationType, data, accumulatedTranslation);
		
		Vector3f scale = sampleVectorChannel(boneAnimation, PAnimationChannel.SCALE, time, interpolationType, data, accumulatedScale);
		
		Quaternionf rotation = sampleRotationChannel(boneAnimation, time, interpolationType, data, accumulatedRotation);
		
		if (translation == null && rotation == null && scale == null)
			return null;
		
		if (translation == null)
			translation = new Vector3f();
		
		if (scale == null)
			scale = new Vector3f(1f, 1f, 1f);
		
		if (rotation == null)
			rotation = new Quaternionf();
		
		return new BoneFrame(translation, rotation, scale);
	}
	
	public List<PAnimationEvent> eventsBetween(float from, float to)
	{
		if (this.events.isEmpty() || to < from)
			return List.of();
		
		return this.events.stream().
				filter(event -> (event.time() > from || (from == 0f && event.time() == 0f)) && event.time() <= to).
				toList();
	}
	
	@SuppressWarnings("unchecked")
	private @Nullable Vector3f sampleVectorChannel(PBoneAnimation boneAnimation, PAnimationChannel channel, float time, PInterpolationType interpolationType, Object data, Vector3f thisValue)
	{
		if (channel == PAnimationChannel.ROTATION)
			return null;
		setThisValue(data, thisValue);
		
		List<PKeyFrameChannel<Vector3f>> keyframes = (List<PKeyFrameChannel<Vector3f>>) boneAnimation.channels().get(channel);
		
		if (keyframes == null || keyframes.isEmpty())
			return null;
		
		if (keyframes.size() == 1)
			return new Vector3f(keyframes.getFirst().value(data));
		
		PKeyFrameChannel<Vector3f> previous = null;
		PKeyFrameChannel<Vector3f> next = null;
		
		for (PKeyFrameChannel<Vector3f> kf : keyframes)
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
			return new Vector3f(keyframes.getFirst().value(data));
		
		if (next == null)
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
		
		Quaternionf q1 = new Quaternionf(previous.value(data));
		Quaternionf q2 = new Quaternionf(next.value(data));
		
		return q1.slerp(q2, alpha);
	}

	private static void setThisValue(Object data, Vector3f value)
	{
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
	}
}
