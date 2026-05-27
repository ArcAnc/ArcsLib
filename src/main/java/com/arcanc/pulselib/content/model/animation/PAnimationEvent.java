/**
 * @author ArcAnc
 * Created at: 27.05.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.model.animation;


import com.arcanc.pulselib.content.animatable.PAnimationEventDispatcher;
import com.arcanc.pulselib.util.PLibDatabase;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import org.joml.Vector3f;

public sealed interface PAnimationEvent permits PAnimationEvent.Sound, PAnimationEvent.Particle
{
	float time();
	
	String locator();
	
	void dispatch(PAnimationEventDispatcher.PositionContext position);
	
	record Sound(float time,
	             ResourceLocation sound,
	             String locator,
	             float volume,
	             float pitch) implements PAnimationEvent
	{
		@Override
		public void dispatch(PAnimationEventDispatcher.PositionContext position)
		{
			SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(this.sound);
			if (sound == null)
			{
				PLibDatabase.LOGGER.warn("Missing animation sound event: {}", this.sound);
				return;
			}
			
			position.level().playLocalSound(
					position.x(),
					position.y(),
					position.z(),
					sound,
					SoundSource.NEUTRAL,
					this.volume,
					this.pitch,
					false);
		}
	}
	
	record Particle(float time,
	                ResourceLocation particle,
	                String locator,
	                Vector3f offset,
	                Vector3f motion) implements PAnimationEvent
	{
		@Override
		public void dispatch(PAnimationEventDispatcher.PositionContext position)
		{
			ParticleType<?> particleType = BuiltInRegistries.PARTICLE_TYPE.get(this.particle());
			if (!(particleType instanceof ParticleOptions particleOptions))
			{
				PLibDatabase.LOGGER.warn("Missing or unsupported simple animation particle: {}", this.particle);
				return;
			}
			
			Vector3f offset = this.offset;
			Vector3f motion = this.motion;
			position.level().addParticle(
					particleOptions,
					position.x() + offset.x(),
					position.y() + offset.y(),
					position.z() + offset.z(),
					motion.x(),
					motion.y(),
					motion.z());
		}
	}
}