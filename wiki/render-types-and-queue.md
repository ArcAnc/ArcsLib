Most mods can use PulseLib's renderers without touching the render queue directly. This page exists for the cases where you need to understand why PulseLib does not use vanilla `RenderType` values and where custom rendering should be inserted.

PulseLib models are triangle meshes loaded from glTF/GLB or another model loader. Vanilla baked block models are mostly quad-based, so PulseLib provides its own triangle render types, shaders, vertex format, and instanced queue. That is why examples use [`PRenderTypes`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/PRenderTypes.java) instead of `RenderType.entityCutout` or block render types.

## Which render type should I choose?

Use the simplest type that matches the visual result:

* `trianglesSolid` for opaque models.
* `trianglesCutout` for hard alpha cutouts, like holes or masked pixels.
* `trianglesTranslucent` for glass-like transparency.
* `trianglesGui` for GUI item rendering.

For emissive meshes the built-in renderers select `trianglesEmissiveCutout` or `trianglesEmissiveTranslucent` automatically from the base type.

In a renderer constructor this usually looks like:

```java
super(modelData, PRenderTypes.RenderTypeProvider::trianglesSolid);
```

If a texture is marked as emissive, the default renderers automatically switch the mesh to the matching emissive variant. You normally do not need to select an emissive type yourself.

Emissive texture metadata is described on [Textures and Emissive](textures-and-emissive.md).

## Why vanilla RenderType is not enough

PulseLib's baked meshes use [`PRenderTypes.VertexFormatProvider.POSITION_TEX_NORMAL`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/PRenderTypes.java). The 26.1 pipelines also expect the `DynamicTransforms`, `Lighting`, and `InstanceData` uniform buffers. A vanilla render type may compile and still render incorrectly because its shader and vertex format do not match the data PulseLib sends.

If you create a custom render type, keep these requirements:

* `VertexFormat.Mode.TRIANGLES`
* `PRenderTypes.VertexFormatProvider.POSITION_TEX_NORMAL`
* a shader that understands PulseLib's uniforms and instance attributes
* a transparency state that matches how the queue should sort the mesh

For most mods, it is safer to start from PulseLib's existing render types and only add a new one when you need a genuinely different shader state.

## What the queue does

[`PRenderQueue`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/PRenderQueue.java) batches identical meshes together and renders many instances with one instanced draw call. That is important for animated block entities and entities: every object can have its own transform and animation pose, but the GPU can still draw repeated mesh buffers efficiently.

The queue has a few stages:

* `SOLID_BLOCKS` for solid block entity meshes.
* `TRANSLUCENT_BLOCKS` for transparent block/entity-adjacent meshes.
* `ENTITIES` for entity and hand-held item rendering.
* `GUI` for GUI rendering.

Normal renderers submit into these stages for you. [`PRenderStagesHandler`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/PRenderStagesHandler.java) handles flushing them at the right time.

## When to submit manually

Manual queue submission is an advanced escape hatch. Use it when you already have a baked PulseLib mesh and want to draw it in the same pipeline.

```java
PRenderQueue.submit(
        PRenderQueue.RenderStage.ENTITIES,
        renderType,
        bakedMesh,
        new PRenderQueue.InstanceData(matrix, 0xFFFFFFFF, packedLight, packedOverlay));
```

`PBakedMesh` owns the GPU vertex and index buffers. Do not pass a vanilla `VertexBuffer`: the queue needs PulseLib's mesh data and its triangle vertex format.

## Renderer hooks

Every PulseLib renderer has the same three-stage flow:

* `preSubmit(PoseStack, renderState, CameraRenderState, SubmitNodeCollector)` runs before the model is submitted.
* `trueSubmit(...)` is the normal PulseLib model submission.
* `postSubmit(...)` runs after the model is submitted.

A typical customization is small:

```java
@Override
public void postSubmit(PoseStack poseStack,
                       RobotRenderState renderState,
                       CameraRenderState cameraRenderState,
                       SubmitNodeCollector submitNodeCollector) {
    // Submit additional PulseLib meshes or collector nodes here.
}
```

Classes used:

* [`PRenderTypes`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/PRenderTypes.java)
* [`PRenderQueue`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/PRenderQueue.java)
* [`PRenderStagesHandler`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/PRenderStagesHandler.java)
