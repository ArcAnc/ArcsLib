Most mods can use PulseLib's renderers without touching the render queue directly. This page exists for the cases where you need to understand why PulseLib does not use vanilla `RenderType` values and where custom rendering should be inserted.

PulseLib models are triangle meshes loaded from glTF/GLB or another model loader. Vanilla baked block models are mostly quad-based, so PulseLib provides its own triangle render types, shaders, vertex format, and instanced queue. That is why examples use [`PRenderTypes`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/util/PRenderTypes.java) instead of `RenderType.entityCutout` or block render types.

## Which render type should I choose?

Use the simplest type that matches the visual result:

* `trianglesSolid` for opaque models.
* `trianglesCutout` for hard alpha cutouts, like holes or masked pixels.
* `trianglesTranslucent` for glass-like transparency.
* `trianglesGui` for direct GUI drawing.
* `trianglesEmissiveCutout` and `trianglesEmissiveTranslucent` for explicit emissive custom rendering.
* `trianglesInstantCutout` and `trianglesInstantTranslucent` for immediate baked-bone drawing.

In a renderer constructor this usually looks like:

```java
super(modelData, PRenderTypes.RenderTypeProvider::trianglesSolid);
```

If a texture is marked as emissive, the default renderers automatically switch the mesh to the matching emissive variant with `PRenderTypes.RenderTypeProvider.emissiveVariant(...)`. You normally do not need to choose an emissive render type yourself unless you are writing custom draw code.

Emissive texture metadata is described on [Textures and Emissive](Textures-and-Emissive).

## Why vanilla RenderType is not enough

PulseLib's baked meshes use [`PRenderTypes.VertexFormatProvider.POSITION_TEX_NORMAL`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/util/PRenderTypes.java). The shaders also expect per-instance data: transform matrix, color, light, and overlay. A vanilla render type may compile and still render incorrectly because its shader and vertex format do not match the data PulseLib sends.

Minecraft 26.2 uses the GPU rendering API for this pipeline. If you create a custom render type, keep these requirements:

* `PrimitiveTopology.TRIANGLES`
* `PRenderTypes.VertexFormatProvider.POSITION_TEX_NORMAL`
* a shader that understands PulseLib's uniforms and instance attributes
* a transparency state that matches how the queue should sort the mesh

Custom pipelines must use the 26.2-style vertex binding and primitive topology (`withVertexBinding(...)` and `withPrimitiveTopology(...)`); `VertexFormat.Mode` is no longer used. For most mods, it is safer to start from PulseLib's existing render types and only add a new one when you need a genuinely different shader state.

## What the queue does

[`PRenderQueue`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/content/renderer/PRenderQueue.java) batches identical meshes together and renders many instances with one instanced draw call. That is important for animated block entities and entities: every object can have its own transform and animation pose, but the GPU can still draw repeated mesh buffers efficiently.

The queue has a few stages:

* `SOLID_BLOCKS` for solid block entity meshes.
* `TRANSLUCENT_BLOCKS` for transparent block/entity-adjacent meshes.
* `ENTITIES` for entity and hand-held item rendering.
* `GUI` for GUI rendering.

Normal renderers submit into these stages for you. [`PRenderStagesHandler`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/content/renderer/PRenderStagesHandler.java) handles flushing them at the right time.

## When to submit manually

Manual queue submission is an advanced escape hatch. Use it when you already have a baked PulseLib mesh and want to draw extra geometry in the same pipeline.

```java
PRenderQueue.submit(
        PRenderQueue.RenderStage.ENTITIES,
        renderType,
        bakedMesh,
        new PRenderQueue.InstanceData(matrix, 0xFFFFFFFF, packedLight, packedOverlay));
```

If you only need to draw a vanilla item, text, a beam, or a simple effect, add your own submit node from `preSubmit` or `postSubmit` with the supplied `SubmitNodeCollector`. That keeps vanilla rendering in vanilla's pipeline and PulseLib mesh rendering in PulseLib's pipeline.

## Renderer hooks

Every PulseLib renderer has the same three-stage flow:

* `preSubmit` runs before the model is submitted.
* `trueSubmit` is the normal PulseLib model submission.
* `postSubmit` runs after the model is submitted.

A typical customization is small:

```java
@Override
public void postSubmit(PoseStack poseStack,
                       PEntityRenderState.LivingImpl<RobotEntity> renderState,
                       CameraRenderState cameraRenderState,
                       SubmitNodeCollector submitNodeCollector) {
    // Add custom submit nodes here, or submit extra PulseLib-compatible meshes.
}
```

Classes used:

* [`PRenderTypes`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/util/PRenderTypes.java)
* [`PRenderQueue`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/content/renderer/PRenderQueue.java)
* [`PRenderStagesHandler`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/content/renderer/PRenderStagesHandler.java)
