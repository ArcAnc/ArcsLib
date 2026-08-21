PulseLib 26.1 renders animated item models in a GUI through [`PItemRenderer`](https://github.com/ArcAnc/PulseLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/PItemRenderer.java). The renderer detects `ItemDisplayContext.GUI`, submits the mesh to `PRenderQueue.RenderStage.GUI`, and registers custom geometry that flushes that stage in the current GUI render pass.

There is no public `PLibHelper.renderModelInGui(...)` helper in this branch. For an item, implement a `PItemRenderer` and let Minecraft invoke its special-model renderer:

```java
public final class WandRenderer extends PItemRenderer<WandItem, WandRenderState> {
    public WandRenderer(PModelData data) {
        super(data, PRenderTypes.RenderTypeProvider::trianglesSolid);
    }

    @Override
    protected WandRenderState createRenderState() {
        return new WandRenderState();
    }
}

public final class WandRenderState extends PItemRenderState.Impl<WandItem> {
}
```

For custom GUI-only geometry, submit a [`PBakedMesh`](https://github.com/ArcAnc/PulseLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/model/baked/PBakedMesh.java) to `PRenderQueue` and flush it from a `SubmitNodeCollector` custom-geometry callback. Keep that code inside a renderer or special-model renderer: it needs the active pose stack, collector, and packed light/overlay values supplied by Minecraft.

`PItemRenderer` always uses the queued, instanced rendering path, including in a GUI. Supply `trianglesSolid`, `trianglesCutout`, or `trianglesTranslucent` according to the material. The `GUI` stage controls when the queue is flushed; it does not require a different shader.

`trianglesGui` is a compatibility alias for `trianglesInstantTranslucent`. It belongs to direct `PBakedBone.instantDraw(...)` rendering and must not be supplied to `PItemRenderer`, because the queued and instant pipelines receive per-draw data through different inputs. A renderer shared by several item display contexts may still select a queued alpha mode per mesh in `resolveMeshRender(...)`.

Classes used:

* [`PItemRenderer`](https://github.com/ArcAnc/PulseLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/PItemRenderer.java)
* [`PItemRenderState`](https://github.com/ArcAnc/PulseLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/base/PItemRenderState.java)
* [`PRenderQueue`](https://github.com/ArcAnc/PulseLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/PRenderQueue.java)
* [`PRenderTypes`](https://github.com/ArcAnc/PulseLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/PRenderTypes.java)
