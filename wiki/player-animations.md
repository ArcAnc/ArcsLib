# Player animations

Player animations modify the vanilla player model after Minecraft has prepared its normal walk, swim, crouch, and item-use pose. They work for both classic and slim skins, including hats, jackets, sleeves, and pants layers.

The API is client-only. Register definitions on the mod event bus through `PulseLibEvents.PlayerAnimationRegistrationEvent`:

```java
@Mod.EventBusSubscriber(modid = ExampleMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT)
public final class ExamplePlayerAnimations {
    private static final PModelData COMBAT_MODEL = new PModelData.Builder(
            ResourceLocation.fromNamespaceAndPath(ExampleMod.MOD_ID, "player/combat"),
            "player",
            PGeckoModelLoader.INSTANCE.id()).build();

    private static final PRawAnimation IDLE = PRawAnimation.begin()
            .thenLoop("animation.combat.idle")
            .build();

    @SubscribeEvent
    public static void register(PulseLibEvents.PlayerAnimationRegistrationEvent event) {
        event.registration().register(
                ResourceLocation.fromNamespaceAndPath(ExampleMod.MOD_ID, "combat_pose"),
                PPlayerAnimationDefinition.builder(COMBAT_MODEL)
                        .when(player -> player.getMainHandItem().is(MyItems.KATANA.get()))
                        .bind(PPlayerPart.BODY, "body")
                        .bind(PPlayerPart.RIGHT_ARM, "right_arm")
                        .mask(PPlayerPart.BODY, PPlayerPart.RIGHT_ARM)
                        .blendMode(PPlayerAnimationBlendMode.ADDITIVE)
                        .weight(0.75f)
                        .controllers(registrar -> registrar.add("combat", () -> state -> {
                            state.controller().play(IDLE);
                            return ControllerState.PLAY;
                        }))
                        .build());
    }
}
```

The example uses the Gecko loader. Register `PGeckoModelLoader.INSTANCE` with `PModelCache` before the first client resource reload, as described in [Model loaders](model-loaders.md), or use a glTF skeleton instead.

Controllers receive a `PPlayerAnimationInstance` as their animatable. Use `state.animatable().player()` to read the current player. PulseLib creates one instance for every `(player UUID, definition id)` pair, so remote players and multiple definitions never share a controller timeline.

Only players currently tracked by the client are ticked. A player visible merely in the multiplayer tab list has no client entity and does not allocate animation state.

For a client packet handler or another explicit client action, use a runtime handle instead of reaching into an animation manager:

```java
PPlayerAnimationHandle animation = PPlayerAnimations.getHandle(player, COMBAT_POSE_ID);
if (animation != null)
    animation.play("combat", ATTACK);
```

`play`, `stop`, `pause`, `resume`, `stopAll`, and state checks address a named controller. `controller(name)` is available for advanced controller-specific operations without exposing an `AnimManagerKey`. Game-state synchronization itself remains the owning mod's responsibility.

## Bindings and masks

`bind(part, boneName)` maps a bone in the animation model to a semantic player part. `PPlayerPart` values are `ROOT`, `HEAD`, `BODY`, `RIGHT_ARM`, `LEFT_ARM`, `RIGHT_LEG`, and `LEFT_LEG`.

`ROOT` applies the bone transform to the complete third-person player render, including its feature layers. Use it for an emote that moves or rotates the whole player, such as a flip. `rootPivot(x, y, z)` selects its rotation pivot in model-space blocks; a value close to `(0, 0.9, 0)` rotates around the centre of a standing player. In first person, `ROOT` and `HEAD` also drive the local camera. An empty off-hand is rendered when its `LEFT_ARM` or `RIGHT_ARM` binding has an active sampled transform.

Each semantic part applies to both the base part and the matching outer skin layer. For example, `RIGHT_ARM` transforms `rightArm` and `rightSleeve` together.

`mask(...)` is an explicit allow-list. It is useful when one skeleton contains more animated bones than a particular definition should own. If it is not supplied, every bound part is enabled. A dynamic mask can decide independently for every part and render frame:

```java
.mask((player, part, partialTick) ->
        part != PPlayerPart.HEAD || !player.isUsingItem())
```

## Blending and weights

Definitions run in ascending `priority`; registrations with the same priority are ordered by their id.

* `ADDITIVE` adds the sampled position and rotation to the pose already produced by vanilla and earlier definitions. This is the default and works well for recoil, breathing, and gestures.
* `REPLACE` blends from the original vanilla pose to the sampled pose. It is suitable for emotes or stances that should override vanilla limb motion.

The definition weight is clamped to `[0, 1]`. A constant weight is sufficient for most cases:

```java
.weight(0.4f)
```

For a dynamic blend, provide a function evaluated every render pass:

```java
.weight((player, partialTick) -> player.isCrouching() ? 1.0f : 0.25f)
```

`partWeight(...)` multiplies that definition weight only for the selected part. It accepts either a constant or the same dynamic weight function:

```java
.weight(0.8f)
.partWeight(PPlayerPart.HEAD, 0.25f)
.partWeight(PPlayerPart.RIGHT_ARM,
        (player, partialTick) -> player.isUsingItem() ? 1.0f : 0.0f)
```

Layer several definitions with different masks, priorities, modes, definition weights, and part weights to combine independent actions.

## Model conventions and limitations

The model only needs a skeleton and animations; mesh data is optional. Bone positions from Gecko animations are converted from blocks to vanilla model pixels automatically. Rotation, position, and scale channels are supported. The skeleton resolver evaluates bind pose and parent transforms; a bound child therefore inherits transforms of intermediate animation bones. A bound `ROOT` is applied once to the render stack and is excluded from child deltas.

The API affects the whole player model in third person and arms in first person. It restores position, rotation, and scale immediately after every draw, preventing a pose from leaking into a different player or another render layer.

`populateMolangContext(...)` can add player-specific Molang queries:

```java
.populateMolangContext((player, instance, controller, context, partialTick) ->
        context.query("is_sneaking", player.isCrouching() ? 1.0f : 0.0f))
```

As with other client-side animation state, multiplayer gameplay events are the owning mod's responsibility to synchronize. The animation definition observes only state available on that client.
