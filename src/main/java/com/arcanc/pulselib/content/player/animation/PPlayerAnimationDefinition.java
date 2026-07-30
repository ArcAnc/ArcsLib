/**
 * @author ArcAnc
 * Created at: 30.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.player.animation;

import com.arcanc.pulselib.content.animatable.PAnimationController;
import com.arcanc.pulselib.content.animatable.PAnimationManager;
import com.arcanc.pulselib.content.renderer.modelData.PModelData;
import com.arcanc.pulselib.data.MolangParser;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3f;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public final class PPlayerAnimationDefinition
{
	private final PModelData modelData;
	private final Predicate<Player> predicate;
	private final Map<PPlayerPart, String> bindings;
	private final Set<PPlayerPart> mask;
	private final PPlayerAnimationMask partMask;
	private final PPlayerAnimationBlendMode blendMode;
	private final PPlayerAnimationWeight weight;
	private final Map<PPlayerPart, PPlayerAnimationWeight> partWeights;
	private final Vector3f rootPivot;
	private final int priority;
	private final ControllerRegistrar controllerRegistrar;
	private final MolangContextProvider molangContextProvider;

	private PPlayerAnimationDefinition(Builder builder)
	{
		this.modelData = builder.modelData;
		this.predicate = builder.predicate;
		this.bindings = Map.copyOf(builder.bindings);
		this.mask = Set.copyOf(builder.mask);
		this.partMask = builder.partMask == null ? PPlayerAnimationMask.of(this.mask) : builder.partMask;
		this.blendMode = builder.blendMode;
		this.weight = builder.weight;
		this.partWeights = Map.copyOf(builder.partWeights);
		this.rootPivot = new Vector3f(builder.rootPivot);
		this.priority = builder.priority;
		this.controllerRegistrar = builder.controllerRegistrar;
		this.molangContextProvider = builder.molangContextProvider;
	}

	public static Builder builder(PModelData modelData)
	{
		return new Builder(modelData);
	}

	public PModelData modelData()
	{
		return this.modelData;
	}

	public boolean shouldApply(Player player)
	{
		return this.predicate.test(player);
	}

	public Map<PPlayerPart, String> bindings()
	{
		return this.bindings;
	}

	public Set<PPlayerPart> mask()
	{
		return this.mask;
	}

	public boolean appliesTo(Player player, PPlayerPart part, float partialTick)
	{
		return this.partMask.contains(player, part, partialTick);
	}

	public PPlayerAnimationBlendMode blendMode()
	{
		return this.blendMode;
	}

	public float weight(Player player, float partialTick)
	{
		return Math.clamp(this.weight.weight(player, partialTick), 0.0f, 1.0f);
	}

	public float weight(Player player, PPlayerPart part, float partialTick)
	{
		return Math.clamp(weight(player, partialTick) * partWeight(player, part, partialTick), 0.0f, 1.0f);
	}

	public float partWeight(Player player, PPlayerPart part, float partialTick)
	{
		PPlayerAnimationWeight partWeight = this.partWeights.getOrDefault(part, PPlayerAnimationWeight.FULL);
		return Math.clamp(partWeight.weight(player, partialTick), 0.0f, 1.0f);
	}
	
	public Vector3f rootPivot()
	{
		return new Vector3f(this.rootPivot);
	}

	public int priority()
	{
		return this.priority;
	}

	void registerControllers(PAnimationManager.PAnimationRegistrar<PPlayerAnimationInstance> registrar)
	{
		this.controllerRegistrar.register(registrar);
	}

	void populateMolangContext(Player player,
	                           PPlayerAnimationInstance instance,
	                           PAnimationController<PPlayerAnimationInstance> controller,
	                           MolangParser.Context context,
	                           float partialTick)
	{
		this.molangContextProvider.populate(player, instance, controller, context, partialTick);
	}

	@FunctionalInterface
	public interface ControllerRegistrar
	{
		ControllerRegistrar EMPTY = registrar -> {};

		void register(PAnimationManager.PAnimationRegistrar<PPlayerAnimationInstance> registrar);
	}

	@FunctionalInterface
	public interface MolangContextProvider
	{
		MolangContextProvider EMPTY = (player, instance, controller, context, partialTick) -> {};

		void populate(Player player,
		              PPlayerAnimationInstance instance,
		              PAnimationController<PPlayerAnimationInstance> controller,
		              MolangParser.Context context,
		              float partialTick);
	}

	public static final class Builder
	{
		private final PModelData modelData;
		private Predicate<Player> predicate = player -> true;
		private final Map<PPlayerPart, String> bindings = new LinkedHashMap<>();
		private final EnumSet<PPlayerPart> mask = EnumSet.noneOf(PPlayerPart.class);
		private PPlayerAnimationMask partMask;
		private PPlayerAnimationBlendMode blendMode = PPlayerAnimationBlendMode.ADDITIVE;
		private PPlayerAnimationWeight weight = PPlayerAnimationWeight.FULL;
		private final Map<PPlayerPart, PPlayerAnimationWeight> partWeights = new LinkedHashMap<>();
		private Vector3f rootPivot = new Vector3f();
		private int priority;
		private ControllerRegistrar controllerRegistrar = ControllerRegistrar.EMPTY;
		private MolangContextProvider molangContextProvider = MolangContextProvider.EMPTY;

		private Builder(PModelData modelData)
		{
			this.modelData = Objects.requireNonNull(modelData);
		}

		public Builder when(Predicate<Player> predicate)
		{
			this.predicate = Objects.requireNonNull(predicate);
			return this;
		}
		
		public Builder bind(PPlayerPart part, String boneName)
		{
			if (boneName == null || boneName.isBlank())
				throw new IllegalArgumentException("Player animation bone name cannot be blank");
			this.bindings.put(Objects.requireNonNull(part), boneName);
			return this;
		}
		
		public Builder mask(PPlayerPart... parts)
		{
			this.mask.clear();
			for (PPlayerPart part : parts)
				this.mask.add(Objects.requireNonNull(part));
			this.partMask = null;
			return this;
		}
		
		public Builder mask(PPlayerAnimationMask mask)
		{
			this.mask.clear();
			this.partMask = Objects.requireNonNull(mask);
			return this;
		}

		public Builder blendMode(PPlayerAnimationBlendMode blendMode)
		{
			this.blendMode = Objects.requireNonNull(blendMode);
			return this;
		}

		public Builder weight(float weight)
		{
			return weight((player, partialTick) -> weight);
		}

		public Builder weight(PPlayerAnimationWeight weight)
		{
			this.weight = Objects.requireNonNull(weight);
			return this;
		}

		public Builder partWeight(PPlayerPart part, float weight)
		{
			return partWeight(part, (player, partialTick) -> weight);
		}

		public Builder partWeight(PPlayerPart part, PPlayerAnimationWeight weight)
		{
			this.partWeights.put(Objects.requireNonNull(part), Objects.requireNonNull(weight));
			return this;
		}
		
		public Builder rootPivot(Vector3f rootPivot)
		{
			this.rootPivot = new Vector3f(Objects.requireNonNull(rootPivot));
			return this;
		}

		public Builder rootPivot(float x, float y, float z)
		{
			return rootPivot(new Vector3f(x, y, z));
		}
		
		public Builder priority(int priority)
		{
			this.priority = priority;
			return this;
		}

		public Builder controllers(ControllerRegistrar controllerRegistrar)
		{
			this.controllerRegistrar = Objects.requireNonNull(controllerRegistrar);
			return this;
		}

		public Builder populateMolangContext(MolangContextProvider provider)
		{
			this.molangContextProvider = Objects.requireNonNull(provider);
			return this;
		}

		public PPlayerAnimationDefinition build()
		{
			if (this.bindings.isEmpty())
				throw new IllegalStateException("A player animation definition needs at least one bone binding");
			if (this.mask.isEmpty() && this.partMask == null)
				this.mask.addAll(this.bindings.keySet());
			return new PPlayerAnimationDefinition(this);
		}
	}
}
