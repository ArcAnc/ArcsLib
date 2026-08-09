/**
 * @author ArcAnc
 * Created at: 05.08.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.model.animation;

import com.arcanc.pulselib.content.model.baked.PBakedModel;
import org.joml.Matrix4f;

import java.util.BitSet;

public final class PModelPose
{
	private final Matrix4f[] transforms;
	private final BitSet validBones = new BitSet();

	public PModelPose(int boneCount)
	{
		this.transforms = new Matrix4f[boneCount];
		for (int index = 0; index < boneCount; index++)
			this.transforms[index] = new Matrix4f();
	}

	public Matrix4f transform(int boneIndex)
	{
		return this.transforms[boneIndex];
	}

	public void update(PBakedModel model, PPose localPose)
	{
		BitSet update = localPose.dirtyBones();
		for (int index = 0; index < this.transforms.length; index++)
		{
			int parent = model.parentIndex(index);
			if (!this.validBones.get(index) || (parent >= 0 && update.get(parent)))
				update.set(index);
		}
		for (int index = update.nextSetBit(0); index >= 0; index = update.nextSetBit(index + 1))
			updateBone(model, localPose, index);
	}

	private void updateBone(PBakedModel model, PPose pose, int index)
	{
		int parent = model.parentIndex(index);
		Matrix4f transform = parent < 0 ? this.transforms[index].identity() : this.transforms[index].set(this.transforms[parent]);
		transform.translate(pose.translation(index)).rotate(pose.rotation(index)).scale(pose.scale(index));
		this.validBones.set(index);
	}
}
