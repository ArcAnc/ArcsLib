/**
 * @author ArcAnc
 * Created at: 30.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.registration;

public final class PPlayerFlipDemo
{
/*	private static final ResourceLocation ID = PLibDatabase.rl("demo/player_flip");
	private static final PModelData MODEL_DATA = new PModelData.Builder(
			PLibDatabase.rl("geckolib/models/player/demo/player_flip.geo.json"),
			"",
			PGeckoModelLoader.INSTANCE.id()).build();
	private static final PRawAnimation FLIP = PRawAnimation.begin().thenPlay("animation.pulselib.player.flip").build();
	private static final KeyMapping FLIP_KEY = new KeyMapping(
			"key.pulselib.player_flip",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_G,
			"key.categories.pulselib");

	private PPlayerFlipDemo()
	{
	}

	public static void register(IEventBus modEventBus)
	{
		if (FMLLoader.isProduction())
			return;

		PModelCache.registerModelLoader(PGeckoModelLoader.INSTANCE);
		modEventBus.addListener(PPlayerFlipDemo :: registerKeyMapping);
		modEventBus.addListener(PPlayerFlipDemo :: registerAnimation);
		NeoForge.EVENT_BUS.addListener(PPlayerFlipDemo :: clientTick);
	}

	private static void registerKeyMapping(RegisterKeyMappingsEvent event)
	{
		event.register(FLIP_KEY);
	}

	private static void registerAnimation(PulseLibEvents.PlayerAnimationRegistrationEvent event)
	{
		event.registration().register(ID, PPlayerAnimationDefinition.builder(MODEL_DATA).
						when(player -> player == Minecraft.getInstance().player).
						bind(PPlayerPart.ROOT, "root").
						bind(PPlayerPart.HEAD, "head").
						bind(PPlayerPart.BODY, "body").
						bind(PPlayerPart.RIGHT_ARM, "right_arm").
						bind(PPlayerPart.LEFT_ARM, "left_arm").
						bind(PPlayerPart.RIGHT_LEG, "right_leg").
						bind(PPlayerPart.LEFT_LEG, "left_leg").
						mask(
							PPlayerPart.ROOT,
							PPlayerPart.HEAD,
							PPlayerPart.BODY,
							PPlayerPart.RIGHT_ARM,
							PPlayerPart.LEFT_ARM,
							PPlayerPart.RIGHT_LEG,
							PPlayerPart.LEFT_LEG).
						rootPivot(0.0f, 0.5f, 0.0f).
						blendMode(PPlayerAnimationBlendMode.REPLACE).
						weight(1.0f).
						controllers(registrar -> registrar.
								add("flip", () -> state ->
										state.controller().isStopped() ? ControllerState.STOP : ControllerState.PLAY)).
						build());
	}

	private static void clientTick(ClientTickEvent.Post event)
	{
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || !FLIP_KEY.consumeClick())
			return;

		PPlayerAnimationHandle animation = PPlayerAnimations.getHandle(minecraft.player, ID);
		if (animation == null)
			return;

		animation.play("flip", FLIP);
	}*/
}
