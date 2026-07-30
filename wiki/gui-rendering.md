PulseLib no longer exposes a `PLibHelper.renderModelInGui` helper. GUI item rendering goes through [`PItemRenderer`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/PItemRenderer.java).

When Minecraft renders a PulseLib item with `ItemDisplayContext.GUI`, `PItemRenderer`:

* extracts the item render state through `PItemRenderState`;
* submits posed meshes to `PRenderQueue.RenderStage.GUI`;
* flushes the GUI stage immediately through the supplied `SubmitNodeCollector`.

That means normal animated items do not need separate GUI drawing code. Use the same item renderer and model data described on [PulseLib Items](PulseLib-Items).

## Direct model drawing

For advanced screens that are not item rendering, draw the baked model bones directly with [`PBakedBone.instantDraw`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/model/baked/PBakedBone.java):

```java
PBakedModel model = modelData.getModel();
if (model != null) {
    poseStack.pushPose();
    poseStack.translate(x, y, 0);
    poseStack.scale(scale, scale, scale);

    for (PBakedBone bone : model.bones()) {
        bone.instantDraw(
                poseStack,
                modelData,
                manager.getControllers().values(),
                PRenderTypes.RenderTypeProvider::trianglesGui,
                0xFFFFFFFF,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                partialTick);
    }

    poseStack.popPose();
}
```

Classes used:

* [`PItemRenderer`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/PItemRenderer.java)
* [`PItemRenderState`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/base/PItemRenderState.java)
* [`PBakedBone`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/model/baked/PBakedBone.java)
* [`PRenderQueue`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/PRenderQueue.java)
