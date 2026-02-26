/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.content.model.animation;


import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

public record ArcAnimation(String name, float length, Map<String, ArcBoneAnimation> boneAnimations)
{
	public @Nullable BoneFrame calculateBoneTransformations(String boneName, float time)
	{
		ArcBoneAnimation boneAnimation = this.boneAnimations.get(boneName);
		if (boneAnimation == null)
			return null;
		
		Vector3f translation =
				sampleVectorChannel(boneAnimation, ArcAnimationChannel.POSITION, time);
		
		Vector3f scale =
				sampleVectorChannel(boneAnimation, ArcAnimationChannel.SCALE, time);
		
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
	private @Nullable Vector3f sampleVectorChannel(@NonNull ArcBoneAnimation boneAnimation, ArcAnimationChannel channel, float time)
	{
		if (channel == ArcAnimationChannel.ROTATION)
			return null;
		
		List<ArcKeyFrameChannel<Vector3f>> keyframes = (List<ArcKeyFrameChannel<Vector3f>>) boneAnimation.channels().get(channel);
		
		if (keyframes == null || keyframes.isEmpty())
			return null;
		
		if (keyframes.size() == 1)
			return new Vector3f(keyframes.getFirst().value());
		
		ArcKeyFrameChannel<Vector3f> previous = null;
		ArcKeyFrameChannel<Vector3f> next = null;
		
		for (ArcKeyFrameChannel<Vector3f> kf : keyframes)
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
	private @Nullable Quaternionf sampleRotationChannel(@NonNull ArcBoneAnimation boneAnimation, float time)
	{
		List<ArcKeyFrameChannel<Quaternionf>> keyframes =
				(List<ArcKeyFrameChannel<Quaternionf>>) boneAnimation.channels().get(ArcAnimationChannel.ROTATION);
		
		if (keyframes == null || keyframes.isEmpty())
			return null;
		
		if (keyframes.size() == 1)
			return keyframes.getFirst().value();
		
		ArcKeyFrameChannel<Quaternionf> previous = null;
		ArcKeyFrameChannel<Quaternionf> next = null;
		
		for (ArcKeyFrameChannel<Quaternionf> kf : keyframes)
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
