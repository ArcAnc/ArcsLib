Most mods can use PulseLib's renderers without touching the render queue directly. This page exists for the cases where you need to understand why PulseLib does not use vanilla `RenderType` values and where custom rendering should be inserted.

PulseLib models are triangle meshes loaded from glTF/GLB or another model loader. Vanilla baked block models are mostly quad-based, so PulseLib provides its own triangle render types, shaders, vertex format, and instanced queue. That is why examples use [`PRenderTypes`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/util/PRenderTypes.java) instead of `RenderType.entityCutout` or block render types.

## Which render type should I choose?

Use the simplest type that matches the visual result:

* `trianglesSolid` for opaque models.
* `trianglesCutout` for hard alpha cutouts, like holes or masked pixels.
* `trianglesTranslucent` for glass-like transparency and weighted OIT.
* `trianglesInstantSolid`, `trianglesInstantCutout`, or `trianglesInstantTranslucent` when rendering an individual mesh immediately.
* `trianglesGui` is retained as a compatibility alias for `trianglesInstantTranslucent`; it is not a separate GUI shader or pipeline.

For emissive meshes the built-in renderers select the matching solid, cutout, or translucent emissive variant automatically from the base type.

Queued built-in translucent variants use a two-layer, depth-peeled weighted order-independent transparency path. PulseLib first finds two transparent depth layers, then accumulates weighted colour and revealage only for fragments belonging to each layer. It composites the farther layer before the nearer one after the entity and translucent-block queues have both flushed, so overlaps within those layers do not depend on submission order. This also means that transparent geometry behind the first two visible layers is not accumulated separately.

The OIT path applies only to PulseLib's queued translucent and emissive-translucent render types when the output target has a depth attachment. Instant/GUI rendering and custom transparent render types retain the queue's back-to-front alpha-blending path. If OIT target allocation or frame setup fails, PulseLib automatically uses that same fallback.

In a renderer constructor this usually looks like:

```java
super(modelData, PRenderTypes.RenderTypeProvider::trianglesSolid);
```

If a texture is marked as emissive, the default renderers automatically switch the mesh to the matching emissive variant. You normally do not need to select an emissive type yourself.

Alpha-mode and emissive texture metadata is described on [Textures and Emissive](textures-and-emissive.md).

## Why vanilla RenderType is not enough

PulseLib's baked meshes use [`PRenderTypes.VertexFormatProvider.POSITION_TEX_NORMAL`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/util/PRenderTypes.java). The 26.2 GPU pipelines use Minecraft's `PrimitiveTopology.TRIANGLES`. Queued variants expect `DynamicTransforms`, `Lighting`, and `InstanceData`; instant variants use `ColorOverlay`. Both paths bind the GPU deformer texel buffers. A vanilla render type may compile and still render incorrectly because its shader and vertex format do not match the data PulseLib sends.

If you create a custom render type, keep these requirements:

* `PrimitiveTopology.TRIANGLES`
* `PRenderTypes.VertexFormatProvider.POSITION_TEX_NORMAL`
* a shader that understands PulseLib's uniforms and instance attributes
* a transparency state that matches how the queue should sort the mesh

For most mods, it is safer to start from PulseLib's existing render types and only add a new one when you need a genuinely different shader state.

## What the queue does

[`PRenderQueue`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/content/renderer/PRenderQueue.java) batches identical opaque meshes together and renders many instances with one instanced draw call (up to 512 instances per draw). Transparent submissions are kept back-to-front so the fallback path remains correct; built-in queued translucent types are redirected to weighted OIT by the executor. Every object can have its own transform, animation pose, and GPU deformer values while repeated mesh buffers remain efficiently batched.

The queue has a few stages:

* `SOLID_BLOCKS` for solid block entity meshes.
* `TRANSLUCENT_BLOCKS` for transparent block/entity-adjacent meshes.
* `ENTITIES` for entity and hand-held item rendering.
* `GUI` for GUI rendering.

Normal renderers submit into these stages for you. [`PRenderStagesHandler`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/content/renderer/PRenderStagesHandler.java) handles flushing them at the right time.

The `ENTITIES` and `TRANSLUCENT_BLOCKS` queues flush at `RenderLevelStageEvent.AfterTranslucentFeatures`; their shared OIT result is composited immediately afterward. Composition processes the two layers from farther to nearer and writes their depth back to the target, so later translucent block rendering can depth-test against the nearest PulseLib transparent layer.

## When to submit manually

Manual queue submission is an advanced escape hatch. Use it when you already have a baked PulseLib mesh and want to draw it in the same pipeline.

```java
PRenderQueue.submit(
        PRenderQueue.RenderStage.ENTITIES,
        renderType,
        bakedMesh,
        null, // or PMeshDeformation
        new PRenderQueue.InstanceData(matrix, 0xFFFFFFFF, packedLight, packedOverlay));
```

`PBakedMesh` owns the GPU vertex and index buffers. Do not pass a vanilla `VertexBuffer`: the queue needs PulseLib's mesh data and its triangle vertex format.

For a `PMeshDeformation`, the queue first selects a cached subdivision level. GPU-supported built-in stacks are represented by offsets in the per-instance data; unsupported stacks use a CPU-deformed vertex buffer for that submission.

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

* [`PRenderTypes`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/util/PRenderTypes.java)
* [`PRenderQueue`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/content/renderer/PRenderQueue.java)
* [`PRenderStagesHandler`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/content/renderer/PRenderStagesHandler.java)
