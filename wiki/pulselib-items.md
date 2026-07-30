Items need a slightly different setup from entities and block entities. Minecraft creates one `Item` object for the item type, but the player can hold many `ItemStack`s of that type. If animation state lived directly on the item object, every stack would share the same animation.

PulseLib handles this by using [`AnimManagerKey`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/animatable/AnimManagerKey.java) and [`SingletonAnimationManager`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/animatable/singleton/SingletonAnimationManager.java). The item still implements [`PItemAnimatable`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/animatable/PItemAnimatable.java), but the actual manager is resolved per key.

## Item JSON

Create a modern item definition file. This is not the animated GLB model; it tells Minecraft to use your registered special model renderer.

```text
assets/<modid>/items/<item>.json
```

```json
{
  "model": {
    "type": "minecraft:special",
    "base": "examplemod:item/wand",
    "model": {
      "type": "examplemod:wand",
      "model_location": "examplemod:glmodels/item/wand.glb",
      "textures": [
        "examplemod:item/wand/body"
      ]
    }
  }
}
```

The `model.type` value, here `examplemod:wand`, must be registered as a special renderer codec. The nested fields are decoded by `PModelData.CODEC`: `model_location`, optional `model_type`, optional `model_format`, and `textures`.

## Item class

The important part here is `getAnimationManager`. Use the key passed into the method, not a single field shared by every stack.

```java
public class WandItem extends Item implements PItemAnimatable<WandItem> {
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

    @Override
    public IClientItemExtensions registerClientExtension() {
        return IClientItemExtensions.DEFAULT;
    }
}
```

## Renderer

The renderer points at the GLB model and selects a PulseLib render type. `PItemRenderer` implements Minecraft's `SpecialModelRenderer<RS>`, so it needs a render-state type and an unbaked codec.

```java
public class WandRenderer extends PItemRenderer<WandItem, PItemRenderState.Impl<WandItem>> {
    public WandRenderer(PModelData modelData) {
        super(modelData, PRenderTypes.RenderTypeProvider::trianglesSolid);
    }

    @Override
    protected PItemRenderState.Impl<WandItem> createRenderState() {
        return new PItemRenderState.Impl<>();
    }

    public record Unbaked(PModelData data) implements SpecialModelRenderer.Unbaked<PItemRenderState.Impl<WandItem>> {
        public static final MapCodec<Unbaked> MAP_CODEC = PModelData.CODEC.xmap(Unbaked::new, Unbaked::data);

        @Override
        public WandRenderer bake(BakingContext context) {
            return new WandRenderer(this.data);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
```

Register the unbaked renderer on the client mod event bus:

```java
@SubscribeEvent
public static void registerSpecialModels(RegisterSpecialModelRendererEvent event) {
    event.register(Identifier.fromNamespaceAndPath("examplemod", "wand"), WandRenderer.Unbaked.MAP_CODEC);
}
```

PulseLib automatically registers item client extensions for items that implement `PItemAnimatable`. Return `IClientItemExtensions.DEFAULT` unless the item also needs extra client extension behavior, such as armor integration.

## Stack-specific state

`AnimManagerKey.of(ItemStack)` uses item id, stack count, and component data. If your item animation should differ by custom data, store it in item components so the key changes. If two stacks have identical key data, they can reuse the same cached manager.

Classes used:

* [`PItemAnimatable`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/animatable/PItemAnimatable.java)
* [`PItemRenderer`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/PItemRenderer.java)
* [`SingletonAnimationManager`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/animatable/singleton/SingletonAnimationManager.java)
* [`DefaultItemModelData`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/modelData/DefaultItemModelData.java)
