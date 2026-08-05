Most mods can use PulseLib's renderers without touching the render queue directly. This page exists for the cases where you need to understand why PulseLib does not use vanilla `RenderType` values and where custom rendering should be inserted.

PulseLib models are triangle meshes loaded from glTF/GLB or another model loader. Vanilla baked block models are mostly quad-based, so PulseLib provides its own triangle render types, shaders, vertex format, and instanced queue. That is why examples use [`PRenderTypes`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/util/PRenderTypes.java) instead of `RenderType.entityCutout` or block render types.

## Which render type should I choose?

Use the simplest type that matches the visual result:

* `trianglesSolid` for opaque models.
* `trianglesCutout` for hard alpha cutouts, like holes or masked pixels.
* `trianglesTranslucent` for glass-like transparency.
* `trianglesGui` for direct GUI drawing.

`trianglesLit` no longer has its own shader. It remains as a compatibility render type backed by the GUI shader, so do not select it for new world rendering; use `trianglesSolid`, `trianglesCutout`, or `trianglesTranslucent` according to the material instead.

In a renderer constructor this usually looks like:

```java
super(modelData, PRenderTypes.RenderTypeProvider::trianglesSolid);
```

If a texture is marked as emissive, the default renderers automatically switch the mesh to the matching emissive variant. You normally do not need to choose `trianglesSolidEmissive` yourself unless you are writing custom draw code.

Emissive texture metadata is described on [Textures and Emissive](Textures-and-Emissive).

## Why vanilla RenderType is not enough

PulseLib's baked meshes use [`PRenderTypes.VertexFormatProvider.POSITION_TEX_NORMAL`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/util/PRenderTypes.java). The shaders also expect per-instance data: transform matrix, color, light, and overlay. A vanilla render type may compile and still render incorrectly because its shader and vertex format do not match the data PulseLib sends.

If you create a custom render type, keep these requirements:

* `VertexFormat.Mode.TRIANGLES`
* `PRenderTypes.VertexFormatProvider.POSITION_TEX_NORMAL`
* a shader that understands PulseLib's uniforms and instance attributes
* a transparency state that matches how the queue should sort the mesh

For most mods, it is safer to start from PulseLib's existing render types and only add a new one when you need a genuinely different shader state.

## What the queue does

[`PRenderQueue`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/content/renderer/PRenderQueue.java) batches identical meshes together and renders many instances with one instanced draw call. That is important for animated block entities and entities: every object can have its own transform and animation pose, but the GPU can still draw repeated mesh buffers efficiently.

The queue has a few stages:

* `SOLID_BLOCKS` for solid block entity meshes.
* `TRANSLUCENT_BLOCKS` for transparent block/entity-adjacent meshes.
* `ENTITIES` for entity and hand-held item rendering.
* `GUI` for GUI rendering.

Normal renderers submit into these stages for you. [`PRenderStagesHandler`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/content/renderer/PRenderStagesHandler.java) handles flushing them at the right time.

## When to submit manually

Manual queue submission is an advanced escape hatch. Use it when you already have a PulseLib-compatible `VertexBuffer` and want to draw extra geometry in the same pipeline.

```java
PRenderQueue.submit(
        PRenderQueue.RenderStage.ENTITIES,
        renderType,
        vertexBuffer,
        new PRenderQueue.InstanceData(matrix, 0xFFFFFFFF, packedLight, packedOverlay));
```

If you only need to draw a vanilla item, text, a beam, or a simple effect, it is often better to use the `MultiBufferSource` passed to `preSubmit` or `postSubmit`. That keeps vanilla rendering in vanilla's pipeline and PulseLib mesh rendering in PulseLib's pipeline.

## Renderer hooks

Every PulseLib renderer has the same three-stage flow:

* `preSubmit` runs before the model is submitted.
* `trueSubmit` is the normal PulseLib model submission.
* `postSubmit` runs after the model is submitted.

A typical customization is small:

```java
@Override
public void postSubmit(PoseStack poseStack,
                       RobotEntity entity,
                       Function<ResourceLocation, RenderType> renderType,
                       MultiBufferSource bufferSource,
                       int packedLight,
                       int packedOverlay,
                       float partialTick,
                       Object... additionalData) {
    // Add vanilla buffer rendering here, or submit extra PulseLib-compatible meshes.
}
```

Classes used:

* [`PRenderTypes`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/util/PRenderTypes.java)
* [`PRenderQueue`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/content/renderer/PRenderQueue.java)
* [`PRenderStagesHandler`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/content/renderer/PRenderStagesHandler.java)
