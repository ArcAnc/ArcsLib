/**
 * @author ArcAnc
 * Created at: 30.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.player.animation;

import com.arcanc.pulselib.content.animatable.AnimManagerKey;
import com.arcanc.pulselib.content.animatable.PAnimatable;
import com.arcanc.pulselib.content.animatable.PAnimationController;
import com.arcanc.pulselib.content.animatable.PAnimationManager;
import com.arcanc.pulselib.content.animatable.instance.InstanceAnimationManager;
import com.arcanc.pulselib.content.model.animation.PAnimationPoseResolver;
import com.arcanc.pulselib.content.model.baked.PBakedModel;
import com.arcanc.pulselib.data.MolangParser;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

public final class PPlayerAnimationInstance implements PAnimatable<PPlayerAnimationInstance>
{
	private Player player;
	private final Identifier id;
	private final PPlayerAnimationDefinition definition;
	private final PAnimationManager<PPlayerAnimationInstance> animationManager;

	PPlayerAnimationInstance(Player player, Identifier id, PPlayerAnimationDefinition definition)
	{
		this.player = Objects.requireNonNull(player);
		this.id = Objects.requireNonNull(id);
		this.definition = Objects.requireNonNull(definition);
		long key = Objects.hash(player.getUUID(), id);
		this.animationManager = new InstanceAnimationManager<>(this, new AnimManagerKey(key));
	}

	public Player player()
	{
		return this.player;
	}

	void updatePlayer(Player player)
	{
		this.player = Objects.requireNonNull(player);
	}

	public Identifier id()
	{
		return this.id;
	}

	public PPlayerAnimationDefinition definition()
	{
		return this.definition;
	}

	@Override
	public PAnimationManager<PPlayerAnimationInstance> getAnimationManager(AnimManagerKey key)
	{
		return this.animationManager;
	}

	@Override
	public void registerAnimationControllers(PAnimationManager.PAnimationRegistrar<PPlayerAnimationInstance> registrar)
	{
		this.definition.registerControllers(registrar);
	}

	@Nullable PAnimationController<PPlayerAnimationInstance> controller(String controllerName)
	{
		return this.animationManager.getControllers().get(controllerName);
	}

	void stopAllControllers()
	{
		this.animationManager.getControllers().values().forEach(PAnimationController :: stop);
	}

	void tick()
	{
		PBakedModel model = this.definition.modelData().getModel();
		if (model == null)
			return;

		this.animationManager.bindModel(model);
		this.animationManager.tick();
	}

	@Nullable PPlayerBonePose sample(String boneName, float partialTick)
	{
		return sample(boneName, partialTick, createMolangContexts(partialTick));
	}

	Map<PAnimationController<PPlayerAnimationInstance>, MolangParser.Context> createMolangContexts(float partialTick)
	{
		Map<PAnimationController<PPlayerAnimationInstance>, MolangParser.Context> contexts = new IdentityHashMap<>();
		for (PAnimationController<PPlayerAnimationInstance> controller : this.animationManager.getControllers().values())
		{
			MolangParser.Context context = new MolangParser.Context().
					query("anim_time", controller.getInterpolatedTime(partialTick) / 20.0f).
					randomSeed(this.animationManager.key().key());
			this.definition.populateMolangContext(this.player, this, controller, context, partialTick);
			contexts.put(controller, context);
		}
		return contexts;
	}

	@Nullable PPlayerBonePose sample(String boneName,
	                                float partialTick,
	                                Map<PAnimationController<PPlayerAnimationInstance>, MolangParser.Context> molangContexts)
	{
		PBakedModel model = this.definition.modelData().getModel();
		if (model == null)
			return null;

		PAnimationPoseResolver<PPlayerAnimationInstance> resolver = new PAnimationPoseResolver<>(
				model,
				this.animationManager.getControllers().values(),
				(controller, tick) -> molangContexts.getOrDefault(controller,
						PAnimationPoseResolver.<PPlayerAnimationInstance>defaultContexts().context(controller, tick)),
				partialTick);
		PAnimationPoseResolver.AnimationDelta pose = resolver.animationDelta(
				boneName,
				this.definition.bindings().get(PPlayerPart.ROOT));
		if (pose == null || !pose.isAnimated())
			return null;

		return new PPlayerBonePose(
				pose.translation(),
				pose.rotation(),
				pose.scale(),
				pose.hasTranslation(),
				pose.hasRotation(),
				pose.hasScale());
	}

	record PPlayerBonePose(Vector3f translation,
	                      Quaternionf rotation,
	                      Vector3f scale,
	                      boolean hasTranslation,
	                      boolean hasRotation,
	                      boolean hasScale)
	{
	}
}
