/**
 * @author ArcAnc
 * Created at: 24.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.content.model.animation;


import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

public class ArcRawAnimation
{
	private final List<AnimationStage> stages;
	
	private ArcRawAnimation(Builder builder)
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
			return then(name, ArcAnimationType.PLAY_ONCE);
		}
		
		public Builder thenLoop(String name)
		{
			return then(name, ArcAnimationType.CYCLE);
		}
		
		public Builder thenHold(String name)
		{
			return then(name, ArcAnimationType.HOLD_LAST_FRAME);
		}
		
		public Builder then(String name, ArcAnimationType type)
		{
			this.stages.add(new AnimationStage(
					name,
					type,
					ArcInterpolationType.LINEAR,
					1.0f,
					0
			));
			return this;
		}
		
		public Builder thenWait(int ticks)
		{
			this.stages.add(new AnimationStage(
					AnimationStage.WAIT,
					ArcAnimationType.PLAY_ONCE,
					ArcInterpolationType.STEP,
					1.0f,
					ticks
			));
			return this;
		}
		
		public Builder withInterpolation(ArcInterpolationType interpolation)
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
		
		public ArcRawAnimation build()
		{
			return new ArcRawAnimation(this);
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
			ArcAnimationType animationType,
			ArcInterpolationType interpolationType,
			float speed,
			int waitTicks
	)
	{
		public static final String WAIT = "arc.internal.wait";
		
		public boolean isWaiting()
		{
			return WAIT.equals(this.animationName());
		}
	}
}
