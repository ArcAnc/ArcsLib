/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.model.animation;


import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;

public record PAnimation(String name, float length, Map<String, PBoneAnimation> boneAnimations)
{
	public @Nullable BoneFrame calculateBoneTransformations(String boneName, float time)
	{
		PBoneAnimation boneAnimation = this.boneAnimations.get(boneName);
		if (boneAnimation == null)
			return null;
		
		Vector3f translation =
				sampleVectorChannel(boneAnimation, PAnimationChannel.POSITION, time);
		
		Vector3f scale =
				sampleVectorChannel(boneAnimation, PAnimationChannel.SCALE, time);
		
		Quaternionf rotation =
				sampleRotationChannel(boneAnimation, time);
		
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
	
	@SuppressWarnings("unchecked")
	private @Nullable Vector3f sampleVectorChannel(PBoneAnimation boneAnimation, PAnimationChannel channel, float time)
	{
		if (channel == PAnimationChannel.ROTATION)
			return null;
		
		List<PKeyFrameChannel<Vector3f>> keyframes = (List<PKeyFrameChannel<Vector3f>>) boneAnimation.channels().get(channel);
		
		if (keyframes == null || keyframes.isEmpty())
			return null;
		
		if (keyframes.size() == 1)
			return new Vector3f(keyframes.getFirst().value());
		
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
			return new Vector3f(keyframes.getFirst().value());
		
		if (next == null)
			return new Vector3f(previous.value());
		
		float alpha = (time - previous.time()) / (next.time() - previous.time());
		
		return new Vector3f(previous.value()).lerp(next.value(), alpha);
	}
	
	@SuppressWarnings("unchecked")
	private @Nullable Quaternionf sampleRotationChannel(PBoneAnimation boneAnimation, float time)
	{
		List<PKeyFrameChannel<Quaternionf>> keyframes =
				(List<PKeyFrameChannel<Quaternionf>>) boneAnimation.channels().get(PAnimationChannel.ROTATION);
		
		if (keyframes == null || keyframes.isEmpty())
			return null;
		
		if (keyframes.size() == 1)
			return keyframes.getFirst().value();
		
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
			return keyframes.getFirst().value();
		
		if (next == null)
			return previous.value();
		
		float alpha =
				(time - previous.time()) /
						(next.time() - previous.time());
		
		Quaternionf q1 = new Quaternionf(previous.value());
		Quaternionf q2 = new Quaternionf(next.value());
		
		return q1.slerp(q2, alpha);
	}
}
