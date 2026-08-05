/**
 * @author ArcAnc
 * Created at: 24.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.animatable;


import com.arcanc.pulselib.content.model.animation.BoneFrame;
import com.arcanc.pulselib.data.gecko.MolangParser;
import com.arcanc.pulselib.content.model.animation.PAnimation;
import com.arcanc.pulselib.content.model.animation.PAnimationGraph;
import com.arcanc.pulselib.content.model.animation.PAnimationGraphRuntime;
import com.arcanc.pulselib.content.model.animation.PCompiledAnimation;
import com.arcanc.pulselib.content.model.animation.PAnimationType;
import com.arcanc.pulselib.content.model.animation.PRawAnimation;
import com.arcanc.pulselib.content.model.baked.PBakedModel;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class PAnimationController<T extends PAnimatable<T>>
{
	protected final String name;
	protected final StateHandler<T> stateHandler;
	
	protected @Nullable PRawAnimation currentAnimation;
	
	private int stageIndex;
	private float time;
	private float prevTime;
	private ControllerState state;
	private final MolangParser.Context persistentMolangContext = new MolangParser.Context();
	private @Nullable PAnimationGraphRuntime graphRuntime;
	
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
	
	public PAnimationController(PAnimationGraph graph)
	{
		this("default", graph);
	}
	
	public PAnimationController(String name, PAnimationGraph graph)
	{
		this(name, state -> ControllerState.PLAY);
		play(graph);
	}
	
	public void play(PAnimationGraph graph)
	{
		this.currentAnimation = null;
		this.stageIndex = 0;
		this.time = 0.0f;
		this.prevTime = 0.0f;
		this.graphRuntime = new PAnimationGraphRuntime(graph);
		this.state = ControllerState.PLAY;
	}

	public @Nullable PAnimationGraphRuntime graphRuntime()
	{
		return this.graphRuntime;
	}

	public PAnimationController<T> setParameter(String name, float value)
	{
		if (this.graphRuntime == null)
			throw new IllegalStateException("This controller does not have an animation graph");
		this.graphRuntime.parameters().set(name, value);
		return this;
	}

	public PAnimationController<T> setParameter(String name, boolean value)
	{
		if (this.graphRuntime == null)
			throw new IllegalStateException("This controller does not have an animation graph");
		this.graphRuntime.parameters().set(name, value);
		return this;
	}

	public PAnimationController<T> trigger(String name)
	{
		if (this.graphRuntime == null)
			throw new IllegalStateException("This controller does not have an animation graph");
		this.graphRuntime.parameters().trigger(name);
		return this;
	}
	
	public void play(PRawAnimation animation)
	{
		this.graphRuntime = null;
		if (this.currentAnimation == animation)
		{
			PRawAnimation.AnimationStage stage = getCurrentStage();
			if (stage != null && stage.animationType() == PAnimationType.HOLD_LAST_FRAME)
				if (this.state == ControllerState.PAUSE)
					return;
			if (this.state == ControllerState.PLAY)
				return;
		}
		
		this.currentAnimation = animation;
		this.stageIndex = 0;
		this.time = 0;
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
		this.prevTime = 0;
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
	
	public @Nullable BoneFrame calculateBoneTransformations(String boneName,
	                                                        PBakedModel model,
	                                                        float partialTick,
	                                                        MolangParser.Context molangContext,
	                                                        @Nullable BoneFrame accumulatedFrame)
	{
		if (this.state == ControllerState.STOP)
			return null;
		
		PRawAnimation.AnimationStage stage = getCurrentStage();
		if (stage == null)
			return null;
		
		PAnimation animation = model.animations().get(stage.animationName());
		
		if (animation == null)
			return null;
		
		float animationTime = this.getInterpolatedTime(partialTick);
		return animation.calculateBoneTransformations(
				boneName,
				animationTime,
				stage.interpolationType(),
				this.persistentMolangContext.copyFrameValuesFrom(molangContext),
				accumulatedFrame);
	}

	public @Nullable BoneFrame calculateBoneTransformations(PCompiledAnimation animation,
	                                                        int boneIndex,
	                                                        float partialTick,
	                                                        MolangParser.Context molangContext,
	                                                        @Nullable BoneFrame accumulatedFrame)
	{
		if (this.state == ControllerState.STOP)
			return null;
		PRawAnimation.AnimationStage stage = getCurrentStage();
		if (stage == null || stage.isWaiting() || animation.boneAnimation(boneIndex) == null)
			return null;
		return animation.animation().calculateBoneTransformations(
				animation.boneAnimation(boneIndex),
				this.getInterpolatedTime(partialTick),
				stage.interpolationType(),
				this.persistentMolangContext.copyFrameValuesFrom(molangContext),
				accumulatedFrame);
	}

	public MolangParser.Context persistentMolangContext()
	{
		return this.persistentMolangContext;
	}

	public void tick(T animatable, float tickCount, PBakedModel model)
	{
		tick(animatable, tickCount, model, List.of(this));
	}
	
	public void tick(T animatable, float tickCount, PBakedModel model, Collection<PAnimationController<T>> poseControllers)
	{
		if (this.graphRuntime != null)
		{
			if (this.state == ControllerState.PLAY)
				this.graphRuntime.tick(model, tickCount);
			return;
		}
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
		
		if (model == null)
			return;
		
		PRawAnimation.AnimationStage stage = this.currentAnimation.getStages().get(this.stageIndex);
		
		if (stage.isWaiting())
		{
			this.prevTime = this.time;
			this.time += tickCount;
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
		this.prevTime = this.time;
		float nextTime = this.time + tickCount * stage.speed();
		switch (stage.animationType())
		{
			case PLAY_ONCE ->
			{
				this.time = nextTime;
				fireEvents(animatable, model, poseControllers, animation, this.prevTime, Math.min(this.time, length));
				if (this.time >= length)
					nextStage();
			}
			case HOLD_LAST_FRAME ->
			{
				this.time = nextTime;
				fireEvents(animatable, model, poseControllers, animation, this.prevTime, Math.min(this.time, length));
				if (this.time >= length)
				{
					this.time = length;
					this.state = ControllerState.PAUSE;
				}
			}
			case CYCLE ->
			{
				if (length > 0)
				{
					if (nextTime >= length)
					{
						fireEvents(animatable, model, poseControllers, animation, this.prevTime, length);
						this.time = nextTime % length;
						fireEvents(animatable, model, poseControllers, animation, 0f, this.time);
					}
					else
					{
						this.time = nextTime;
						fireEvents(animatable, model, poseControllers, animation, this.prevTime, this.time);
					}
				}
				else
					this.time = nextTime;
			}
		}
	}
	
	private void fireEvents(T animatable,
	                        PBakedModel model,
	                        Collection<PAnimationController<T>> poseControllers,
	                        PAnimation animation,
	                        float from,
	                        float to)
	{
		animation.eventsBetween(from, to).
				forEach(event -> PAnimationEventDispatcher.dispatch(animatable, event, model, poseControllers));
	}
	
	private void nextStage()
	{
		this.stageIndex++;
		this.time = 0;
		this.prevTime = 0;
		
		if (this.currentAnimation == null)
		{
			this.state = ControllerState.STOP;
			return;
		}
		
		if (this.stageIndex >= this.currentAnimation.getStages().size())
			this.state = ControllerState.STOP;
	}
	
	public float getInterpolatedTime(float partialTick)
	{
		if (this.graphRuntime != null)
			return this.graphRuntime.interpolatedTime(partialTick);
		if  (this.time < this.prevTime)
			return Mth.lerp(partialTick, this.prevTime, this.prevTime + this.time);
		return Mth.lerp(partialTick, this.prevTime, this.time);
	}
	
	public float getTime()
	{
		if (this.graphRuntime != null)
			return this.graphRuntime.time();
		return this.time;
	}
	
	public float cyclePhase(PBakedModel model)
	{
		if (this.graphRuntime != null)
			return this.graphRuntime.cyclePhase(model);
		PRawAnimation.AnimationStage stage = getCurrentStage();
		if (stage == null || stage.animationType() != PAnimationType.CYCLE)
			return Float.NaN;
		PAnimation animation = model.animations().get(stage.animationName());
		return animation == null || animation.length() <= 0.0f ? Float.NaN : this.time / animation.length();
	}
	
	public void syncCycle(PBakedModel model, float phase)
	{
		if (this.graphRuntime != null)
		{
			this.graphRuntime.syncCycle(model, phase);
			return;
		}
		PRawAnimation.AnimationStage stage = getCurrentStage();
		if (stage == null || stage.animationType() != PAnimationType.CYCLE)
			return;
		PAnimation animation = model.animations().get(stage.animationName());
		if (animation == null || animation.length() <= 0.0f)
			return;
		this.time = Math.clamp(phase, 0.0f, 1.0f) * animation.length();
		this.prevTime = this.time;
	}
	
	public PRawAnimation.@Nullable AnimationStage getCurrentStage()
	{
		if (this.currentAnimation == null)
			return null;
		if (this.stageIndex >= this.currentAnimation.getStages().size())
			return null;
		return this.currentAnimation.getStages().get(this.stageIndex);
	}

	public List<PAnimationGraphRuntime.Layer> graphLayers(PBakedModel model)
	{
		if (this.graphRuntime == null || this.state == ControllerState.STOP)
			return List.of();
		return this.graphRuntime.layers(model);
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
