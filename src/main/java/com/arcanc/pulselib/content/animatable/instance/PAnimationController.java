/**
 * @author ArcAnc
 * Created at: 24.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.animatable.instance;


import com.arcanc.pulselib.content.animatable.PAnimatable;
import com.arcanc.pulselib.content.model.animation.PAnimation;
import com.arcanc.pulselib.content.model.animation.PRawAnimation;
import com.arcanc.pulselib.content.model.animation.BoneFrame;
import com.arcanc.pulselib.content.model.baked.PBakedModel;
import org.jetbrains.annotations.Nullable;

public class PAnimationController<T extends PAnimatable<T>>
{
	protected final String name;
	protected final StateHandler<T> stateHandler;
	
	protected @Nullable PRawAnimation currentAnimation;
	
	private int stageIndex;
	private float time;
	private ControllerState state;
	
	public PAnimationController(StateHandler<T> stateHandler)
	{
		this("default", stateHandler);
	}
	
	public PAnimationController(String name, StateHandler<T> stateHandler)
	{
		this.name = name;
		this.stateHandler = stateHandler;
		this.state = ControllerState.STOP;
	}
	
	public void play(PRawAnimation animation)
	{
		this.currentAnimation = animation;
		if (!this.isPlaying())
		{
			this.stageIndex = 0;
			this.time = 0;
		}
		this.state = ControllerState.PLAY;
	}
	
	public void pause()
	{
		if (this.state == ControllerState.PLAY)
			this.state = ControllerState.PAUSE;
	}
	
	public void resume()
	{
		if (this.state == ControllerState.PAUSE)
			this.state = ControllerState.PLAY;
	}
	
	public void stop()
	{
		this.currentAnimation = null;
		this.stageIndex = 0;
		this.time = 0;
		this.state = ControllerState.STOP;
	}
	
	public ControllerState getState()
	{
		return this.state;
	}
	
	public boolean isPlaying()
	{
		return this.state == ControllerState.PLAY;
	}
	
	public boolean isPaused()
	{
		return this.state == ControllerState.PAUSE;
	}
	
	public boolean isStopped()
	{
		return this.state == ControllerState.STOP;
	}
	
	public @Nullable BoneFrame calculateBoneTransformations(String boneName, PBakedModel model)
	{
		if (this.state == ControllerState.STOP)
			return null;
		
		PRawAnimation.AnimationStage stage = getCurrentStage();
		if (stage == null)
			return null;
		
		PAnimation animation = model.animations().get(stage.animationName());
		
		if (animation == null)
			return null;
		
		return animation.calculateBoneTransformations(boneName, this.time);
	}
	
	public void tick(T animatable, PBakedModel model, float partialTick)
	{
		ControllerState newState = this.stateHandler.handle(new AnimatableState<>(animatable, this));
		if (newState != this.state)
			this.state = newState;
		
		if (this.state != ControllerState.PLAY)
			return;
		
		if (this.currentAnimation == null)
		{
			this.state = ControllerState.STOP;
			return;
		}
		
		if (this.stageIndex >= this.currentAnimation.getStages().size())
		{
			this.state = ControllerState.STOP;
			return;
		}
		
		PRawAnimation.AnimationStage stage = this.currentAnimation.getStages().get(this.stageIndex);
		
		if (stage.isWaiting())
		{
			this.time += partialTick;
			if (this.time >= stage.waitTicks())
				nextStage();
			return;
		}
		
		PAnimation animation = model.animations().get(stage.animationName());
		if (animation == null)
		{
			nextStage();
			return;
		}
		
		float length = animation.length();
		this.time += partialTick * stage.speed();
		switch (stage.animationType())
		{
			case PLAY_ONCE ->
			{
				if (this.time >= length)
				{
					nextStage();
				}
				
			}
			case HOLD_LAST_FRAME ->
			{
				if (this.time >= length)
				{
					this.time = length;
					this.state = ControllerState.PAUSE;
				}
			}
			case CYCLE ->
			{
				if (length > 0)
					this.time %= length;
			}
		}
	}
	
	private void nextStage()
	{
		this.stageIndex++;
		this.time = 0;
		
		if (this.currentAnimation == null)
		{
			this.state = ControllerState.STOP;
			return;
		}
		
		if (this.stageIndex >= this.currentAnimation.getStages().size())
			this.state = ControllerState.STOP;
	}
	
	public float getTime()
	{
		return this.time;
	}
	
	public PRawAnimation.@Nullable AnimationStage getCurrentStage()
	{
		if (this.currentAnimation == null)
			return null;
		if (this.stageIndex >= this.currentAnimation.getStages().size())
			return null;
		return this.currentAnimation.getStages().get(this.stageIndex);
	}
	
	@FunctionalInterface
	public interface StateHandler<T extends PAnimatable<T>>
	{
		ControllerState handle(AnimatableState<T> state);
	}
	
	public String name()
	{
		return this.name;
	}
	
	public record AnimatableState<T extends PAnimatable<T>>(T animatable, PAnimationController<T> controller)
	{
	}
}
