/**
 * @author ArcAnc
 * Created at: 24.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.model.animation;


import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

public class PRawAnimation
{
	private final List<AnimationStage> stages;
	
	private PRawAnimation(Builder builder)
	{
		this.stages = List.copyOf(builder.stages);
	}
	
	public static Builder begin()
	{
		return new Builder();
	}
	
	public List<AnimationStage> getStages()
	{
		return this.stages;
	}
	
	public static class Builder
	{
		private final List<AnimationStage> stages = new ObjectArrayList<>();
		
		public Builder thenPlay(String name)
		{
			return then(name, PAnimationType.PLAY_ONCE);
		}
		
		public Builder thenLoop(String name)
		{
			return then(name, PAnimationType.CYCLE);
		}
		
		public Builder thenHold(String name)
		{
			return then(name, PAnimationType.HOLD_LAST_FRAME);
		}
		
		public Builder then(String name, PAnimationType type)
		{
			this.stages.add(new AnimationStage(
					name,
					type,
					PInterpolationType.LINEAR,
					1.0f,
					0
			));
			return this;
		}
		
		public Builder thenWait(int ticks)
		{
			this.stages.add(new AnimationStage(
					AnimationStage.WAIT,
					PAnimationType.PLAY_ONCE,
					PInterpolationType.STEP,
					1.0f,
					ticks
			));
			return this;
		}
		
		public Builder withInterpolation(PInterpolationType interpolation)
		{
			AnimationStage last = lastStage();
			
			replaceLast(new AnimationStage(
					last.animationName(),
					last.animationType(),
					interpolation,
					last.speed(),
					last.waitTicks()
			));
			
			return this;
		}
		
		public Builder withSpeed(float speed)
		{
			if (!Float.isFinite(speed))
				throw new IllegalArgumentException("Animation stage speed must be finite");
			AnimationStage last = lastStage();
			
			replaceLast(new AnimationStage(
					last.animationName(),
					last.animationType(),
					last.interpolationType(),
					speed,
					last.waitTicks()
			));
			
			return this;
		}
		
		public PRawAnimation build()
		{
			return new PRawAnimation(this);
		}
		
		private AnimationStage lastStage()
		{
			if (this.stages.isEmpty())
				throw new IllegalStateException("No animation stages defined");
			
			return this.stages.getLast();
		}
		
		private void replaceLast(AnimationStage stage)
		{
			this.stages.set(this.stages.size() - 1, stage);
		}
	}
	
	public record AnimationStage(
			String animationName,
			PAnimationType animationType,
			PInterpolationType interpolationType,
			float speed,
			int waitTicks
	)
	{
		public static final String WAIT = "pulse.internal.wait";
		
		public boolean isWaiting()
		{
			return WAIT.equals(this.animationName());
		}
	}
}
