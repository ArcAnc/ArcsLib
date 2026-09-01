# PulseLib Items

Items need a slightly different setup from entities and block entities. Minecraft creates one `Item` object for the item type, but the player can hold many `ItemStack`s of that type. If animation state lived directly on the item object, every stack would share the same animation.

PulseLib handles this by using [`AnimManagerKey`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/content/animatable/AnimManagerKey.java) and [`SingletonAnimationManager`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/content/animatable/singleton/SingletonAnimationManager.java). The item implements [`PAnimatable`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/content/animatable/PAnimatable.java), while its manager is resolved per key.

## Item model definition

The old `models/item/<item>.json` file with `"parent": "builtin/entity"` does not select a PulseLib renderer on 26.2. Register a `SpecialModelRenderer.Unbaked` codec, then reference that registered special-model type from the item's 26.2 model definition. The wrapper below is the PulseLib side of that setup.

## Item class

The important part here is `getAnimationManager`. Use the key passed into the method, not a single field shared by every stack.

```java
public class WandItem extends Item implements PAnimatable<WandItem> {
    private static final PRawAnimation IDLE = PRawAnimation.begin()
            .thenLoop("idle")
            .build();

    public WandItem(Properties properties) {
        super(properties);
    }

    @Override
    public PAnimationManager<WandItem> getAnimationManager(AnimManagerKey key) {
        return SingletonAnimationManager.getManager(key, this);
    }

    @Override
    public void registerAnimationControllers(PAnimationManager.PAnimationRegistrar<WandItem> registrar) {
        registrar.add("idle", () -> state -> {
            state.controller().play(IDLE);
            return ControllerState.PLAY;
        });
    }
}
```

## Renderer

The 26.2 renderer is a `SpecialModelRenderer`, not a `BlockEntityWithoutLevelRenderer`. It points at model data, selects a PulseLib render type, and creates a render state for every item render.

```java
public class WandRenderer extends PItemRenderer<WandItem, WandRenderState> {
    public WandRenderer(PModelData data) {
        super(data, PRenderTypes.RenderTypeProvider::trianglesSolid);
    }

    @Override
    protected WandRenderState createRenderState() {
        return new WandRenderState();
    }
}

public class WandRenderState extends PItemRenderState.Impl<WandItem> {}
```

When rendered in a GUI, `PItemRenderer` draws through the collector's immediate path with `trianglesInstantTranslucent` as its default material; the constructor's render type applies to non-GUI item contexts. To choose the GUI pipeline for a specific mesh, override `resolveMeshRender(...)` and return `inherited.withAlphaMode(...)`. This selects the matching instant solid, cutout, or translucent variant; emissive meshes automatically use their instant emissive counterpart.

Provide an `Unbaked` special-model wrapper that bakes this renderer:

```java
public record Unbaked(PModelData data)
        implements SpecialModelRenderer.Unbaked<WandRenderState> {
    public static final MapCodec<Unbaked> MAP_CODEC = PModelData.CODEC.xmap(Unbaked::new, Unbaked::data);

    @Override
    public WandRenderer bake(BakingContext context) {
        return new WandRenderer(data);
    }

    @Override
    public MapCodec<Unbaked> type() {
        return MAP_CODEC;
    }
}
```

Register that unbaked codec with NeoForge's special-model renderer registration and reference it from the item's 26.2 model definition. PulseLib does not convert a legacy `builtin/entity` JSON into this renderer automatically.

`PItemAnimatable` no longer exists, and animated item rendering needs no `IClientItemExtensions` registration. If the item needs an unrelated client extension, register it through NeoForge's normal `RegisterClientExtensionsEvent` handling.

## Stack-specific state

`AnimManagerKey.of(ItemStack)` uses item id, stack count, and component data. If your item animation should differ by custom data, store it in item components so the key changes. If two stacks have identical key data, they can reuse the same cached manager.

Classes used:

* [`PAnimatable`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/content/animatable/PAnimatable.java)
* [`PItemRenderer`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/content/renderer/PItemRenderer.java)
* [`SingletonAnimationManager`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/content/animatable/singleton/SingletonAnimationManager.java)
* [`DefaultItemModelData`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/content/renderer/modelData/DefaultItemModelData.java)
