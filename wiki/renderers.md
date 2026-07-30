A PulseLib renderer is the bridge between Minecraft's render call and PulseLib's baked animated model. It gets the current animatable, asks for model data, binds the animation manager to the baked model, and submits every posed bone to the render queue.

All built-in renderers implement [`PRenderer`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/PRenderer.java) and use the same submit lifecycle:

* `preSubmit(...)` - hook before PulseLib submits model meshes.
* `trueSubmit(...)` - default PulseLib model submission.
* `postSubmit(...)` - hook after model submission.

The current renderer API is render-state based. `PBlockRenderer`, `PItemRenderer`, `PEntityRenderer`, and `PEntityRenderLayer` all take a render-state type parameter, and hook methods receive that render state rather than the raw animatable.

Most custom renderers only need a constructor and `createRenderState()`. Override `preSubmit`, `resolveMeshRender`, or `postSubmit` when you need to draw something extra around the PulseLib model or change mesh color/light/overlay.

## Molang context

The built-in entity, block entity, and item renderers prepare a Molang context once per controller and render pass, then reuse it for all mixed bones. Override `populateMolangContext(...)` to add renderer-specific `query.*` values. The full API and the current `variable.*` lifetime limitation are documented in [Molang animations](molang-animations.md).

## Block renderer

Use [`PBlockRenderer`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/PBlockRenderer.java) for block entities. The block itself should hide vanilla rendering; the block entity renderer becomes the visible model.

```java
public class CrusherRenderer extends PBlockRenderer<CrusherBlockEntity, PBlockRenderState.Impl<CrusherBlockEntity>> {
    public CrusherRenderer(BlockEntityRendererProvider.Context context) {
        super(new DefaultBlockModelData.DefaultBlockModelDataBuilder(
                        Identifier.fromNamespaceAndPath("examplemod", "crusher"))
                        .build(),
                PRenderTypes.RenderTypeProvider::trianglesCutout);
    }

    @Override
    public PBlockRenderState.Impl<CrusherBlockEntity> createRenderState() {
        return new PBlockRenderState.Impl<>();
    }
}
```

Register it through NeoForge:

```java
@SubscribeEvent
public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerBlockEntityRenderer(MyBlockEntities.CRUSHER.get(), CrusherRenderer::new);
}
```

## Item renderer

Use [`PItemRenderer`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/PItemRenderer.java) when the item model needs real animation instead of a static baked item JSON. The item must implement [`PItemAnimatable`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/animatable/PItemAnimatable.java) and return client extensions.

```java
public class WandRenderer extends PItemRenderer<WandItem, PItemRenderState.Impl<WandItem>> {
    public WandRenderer() {
        super(new DefaultItemModelData.DefaultItemModelDataBuilder(
                        Identifier.fromNamespaceAndPath("examplemod", "wand"))
                        .build(),
                PRenderTypes.RenderTypeProvider::trianglesSolid);
    }

    @Override
    protected PItemRenderState.Impl<WandItem> createRenderState() {
        return new PItemRenderState.Impl<>();
    }
}
```

`PItemRenderer` implements Minecraft's `SpecialModelRenderer<RS>`. Register an unbaked special renderer codec and point the item JSON at that renderer type. In GUI context, `PItemRenderer` submits to `PRenderQueue.RenderStage.GUI` and flushes that stage immediately.

## Entity renderer

Use [`PEntityRenderer`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/PEntityRenderer.java) when the entire entity model is a PulseLib model.

```java
public class RobotRenderer extends PEntityRenderer<RobotEntity, PEntityRenderState.LivingImpl<RobotEntity>> {
    public RobotRenderer(EntityRendererProvider.Context context) {
        super(context,
                new DefaultEntityModelData.DefaultEntityModelDataBuilder(
                        Identifier.fromNamespaceAndPath("examplemod", "robot")).build(),
                PRenderTypes.RenderTypeProvider::trianglesSolid);
    }

    @Override
    public PEntityRenderState.LivingImpl<RobotEntity> createRenderState() {
        return PLibHelper.livingRenderState();
    }
}
```

For `LivingEntity` subclasses, `PEntityRenderer` applies vanilla body rotation, sleeping/death/spin transforms, entity scale, and head yaw/pitch for a bone named `head`.

## Render type requirement

PulseLib models are triangle meshes. Use [`PRenderTypes.RenderTypeProvider`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/PRenderTypes.java):

```java
PRenderTypes.RenderTypeProvider::trianglesSolid
PRenderTypes.RenderTypeProvider::trianglesCutout
PRenderTypes.RenderTypeProvider::trianglesTranslucent
PRenderTypes.RenderTypeProvider::trianglesGui
PRenderTypes.RenderTypeProvider::trianglesEmissiveCutout
PRenderTypes.RenderTypeProvider::trianglesEmissiveTranslucent
PRenderTypes.RenderTypeProvider::trianglesInstantCutout
PRenderTypes.RenderTypeProvider::trianglesInstantTranslucent
```

Do not pass vanilla entity/block `RenderType` values unless they use a compatible triangle vertex format and shader setup.
