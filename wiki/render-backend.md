# Render Backend

PulseLib separates model traversal from OpenGL execution. Renderers evaluate animation, walk baked bones, and submit mesh instances; the backend later compiles those submissions and draws them at the matching NeoForge render stage.

```text
renderer
  -> PRenderQueue
  -> PFrameCompiler
  -> PRenderPlan
  -> GlDrawExecutor
  -> geometry arena / dynamic VertexBuffer
```

Most integrations should use the built-in renderers and `PRenderTypes`. The classes on this page are primarily useful when implementing custom geometry submission or diagnosing backend behavior.

## Resource handles

`GlResourceRegistry` translates backend-neutral handles into legacy OpenGL resources:

* `PPipelineHandle` identifies a Minecraft `RenderType`;
* `PMeshHandle` identifies static `PGeometryData` or dynamic `GlDynamicGeometry`;
* `PDrawCommand` describes index count, instance count, first index, base vertex, and first instance.

Handles keep `PRenderPlan` independent of concrete OpenGL object IDs. They remain valid until `PRenderQueue.cleanup()` or a model resource reload clears the registry.

## Static geometry arena

Baked meshes normally expose immutable `PGeometryData`. On first submission, `GlGeometryArena` appends its vertex and index bytes to a page and returns a slice containing:

* the page VAO;
* byte offset in the shared index buffer;
* OpenGL index type;
* base vertex in the shared vertex buffer.

A normal page reserves 4 MiB for vertices and 4 MiB for indices. Larger meshes receive a page sized to fit. Index data is aligned to its 2-byte or 4-byte element size, so unsigned-short and unsigned-int slices can share a page.

The local indices of every mesh remain unchanged. `baseVertex` relocates them into the shared vertex buffer, while `indexOffset` or `firstIndex` locates their range in the shared index buffer. Direct draws use the byte offset; indirect commands use `firstIndex`, measured in index elements.

All geometry placed in the current arena must use `PRenderTypes.VertexFormatProvider.POSITION_TEX_NORMAL` and its stride. A page owns one VAO layout, so mixing arbitrary vertex layouts in the same arena is unsupported.

The arena is append-only. Individual slices are not removed or compacted; all pages are released together during cleanup or resource reload. This matches baked models, whose geometry is stable between reloads.

## Dynamic geometry

CPU-deformed meshes use `PDynamicGeometry`, backed by a Minecraft `VertexBuffer`. They are not copied into the static arena because their vertex contents may change. Dynamic geometry can still be drawn with instancing, but it cannot share a page VAO and index buffer with unrelated meshes, which limits multi-draw batching.

Built-in GPU-compatible deformers keep the original mesh in the static arena. Their operation definitions and current values are supplied separately to the shader; see [Mesh Deformers](mesh-deformers.md) and [Shaders](shaders.md).

## Frame compilation

`PFrameCompiler` collects submissions per render stage.

Opaque submissions are grouped by pipeline, mesh, and draw command. Every compatible occurrence becomes another `PInstanceHeader` in one draw group, allowing a repeated mesh to be drawn as instances.

Transparent submissions are initially kept individually and sorted back-to-front using the translation in their instance transform. Adjacent submissions with the same draw key are then combined without changing that ordering. The sorted order is also retained as the fallback when weighted OIT is unavailable.

Each `PInstanceHeader` contains:

* a copied affine transform;
* packed color;
* packed block/sky light;
* packed overlay;
* GPU-deformer operation offset, value offset, and operation count.

## Execution and capability fallbacks

`GlDrawExecutor` probes the active OpenGL context the first time it executes a non-empty plan. Texture-buffer support is required by the current deformer storage path. Other capabilities select optimizations:

| Capability | Selected behavior | Fallback |
| --- | --- | --- |
| Multi-draw indirect plus base instance | Multiple arena slices in one indirect call | Direct instanced draws |
| Buffer storage | Persistently mapped ring buffers | `glBufferData` uploads |
| Independent draw-buffer blending | Weighted blended OIT | Sorted alpha blending |

The instance record occupies 96 bytes. It contains three transform rows, color, light, overlay, three deformer integers, and padding. The executor uploads all records for a plan into one instance buffer.

Persistent uploads use a nine-slot ring: three frames in flight multiplied by three expected stage submissions per frame. `GlFrameArena` attaches a fence to a completed slot and does not reuse it until the GPU signals completion. Growing or replacing a persistent buffer first waits for outstanding slots.

## Draw batching

Opaque groups are already ordered by pipeline. With multi-draw indirect enabled, the executor further partitions arena geometry by `(VAO, indexType)`. Every partition can be issued with one `glMultiDrawElementsIndirect` because its commands share the pipeline, vertex layout, and bound index buffer.

Transparent groups are only combined when compatible groups are adjacent. This avoids arbitrary regrouping that would invalidate fallback alpha ordering.

For a direct arena draw, the executor calls `glDrawElementsInstancedBaseVertex`. For dynamic geometry it binds the mesh's own `VertexBuffer` and reads its primitive mode and index type through the Minecraft accessor mixin.

## Shader execution

Before drawing, the executor applies the group's `RenderType`, binds instance attributes, sets Minecraft's default matrices and light directions, and binds the deformer texture buffers. The queued shader contract and its immediate counterpart are documented on [Shaders](shaders.md).

Built-in translucent render types are separated from standard groups. When weighted OIT starts successfully, they render into persistent per-frame accumulation and revealage attachments. The attachments are resolved once after weather, into the Fabulous weather target when available or the main target otherwise. If setup fails, the affected groups are drawn through their ordinary translucent shader without losing the compiled fallback ordering.

## Render stages and cleanup

`PRenderStagesHandler` flushes:

* entity and non-GUI item submissions after entities;
* opaque block-entity submissions after block entities;
* translucent block submissions after particles;
* the accumulated OIT target after weather, before Minecraft resolves its Fabulous transparency chain.

The final translucent stage also finishes the per-frame GPU deformer streams. Callers should not flush the standard stages or reset those streams manually.

`PRenderQueue.cleanup()` clears pending compiler state, waits for in-flight buffer slots, deletes executor buffers and OIT targets, and releases every registered geometry page. Model reload and client-level unload invoke this lifecycle for normal library use.
