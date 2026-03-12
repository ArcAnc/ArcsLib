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
	T value();
	
	record PositionKeyFrame(float time, Vector3f value) implements PKeyFrameChannel<Vector3f>
	{
	}
	
	record RotationKeyFrame(float time, Quaternionf value) implements PKeyFrameChannel<Quaternionf>
	{
	}
	
	record ScaleKeyFrame(float time, Vector3f value) implements PKeyFrameChannel<Vector3f>
	{
	}
}
