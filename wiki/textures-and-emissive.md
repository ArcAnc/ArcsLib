# Textures and Emissive

PulseLib does not draw model textures directly from arbitrary files. It first collects them into a runtime atlas, then the baked model stores UVs for that atlas. This is why texture registration is a required step instead of an optional convenience.

Texture registration uses [`PulseLibEvents.RegisterTextureEvent`](https://github.com/ArcAnc/PulseLib/blob/1.21.1/src/main/java/com/arcanc/pulselib/content/event/PulseLibEvents.java).

## Register textures

Subscribe on the mod event bus and add every texture that a PulseLib model may use:

```java
@Mod.EventBusSubscriber(modid = ExampleMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ExampleClientEvents {
    @SubscribeEvent
    public static void registerPulseTextures(PulseLibEvents.RegisterTextureEvent event) {
        event.addTextureLocation(ResourceLocation.fromNamespaceAndPath(
                ExampleMod.MOD_ID, "entity/robot/body"));
        event.addTextureLocation(ResourceLocation.fromNamespaceAndPath(
                ExampleMod.MOD_ID, "entity/robot/eyes"));
    }
}
```

The most common mistake is to include too much of the file path. Resource locations are relative to `textures` and have no `.png` extension:

```text
assets/examplemod/textures/entity/robot/body.png
```

becomes:

```java
ResourceLocation.fromNamespaceAndPath("examplemod", "entity/robot/body")
```

## Runtime atlas

The atlas is registered by PulseLib itself. Your mod only contributes texture locations.

Runtime atlas classes:

* [`PTextureCache`](https://github.com/ArcAnc/PulseLib/blob/1.21.1/src/main/java/com/arcanc/pulselib/util/PTextureCache.java)
* [`RuntimeLoader`](https://github.com/ArcAnc/PulseLib/blob/1.21.1/src/main/java/com/arcanc/pulselib/content/model/textures/atlas/RuntimeLoader.java)

PulseLib registers the atlas at:

```java
PTextureCache.ATLAS_LOCATION // pulselib:textures/atlas.png
```

Renderers normally pass `PTextureCache.ATLAS_LOCATION` to `PRenderTypes`, so you rarely need to access the atlas manually.

## Alpha modes

PulseLib can classify an atlas sprite and select a matching queued render type. The available modes are:

* `opaque` - always use `trianglesSolid`;
* `cutout` - use `trianglesCutout` and discard fragments below the shader's `0.1` alpha threshold;
* `translucent` - use `trianglesTranslucent`, including weighted OIT when supported;
* `auto` - inspect the sprite pixels when the model is baked.

The automatic classifier chooses `opaque` when every pixel has alpha 255, `cutout` when alpha contains only 0 and 255, and `translucent` when any pixel has an alpha value from 1 through 254. Results are cached for the lifetime of the loaded atlas.

You can set the mode next to the texture's emissive metadata:

```json
{
  "pulselib": {
    "alpha_mode": "auto",
    "emissive": false
  }
}
```

The render type supplied to a renderer constructor remains authoritative unless a render resolver opts into alpha-mode selection. Use `PAlphaMode.AUTO` to apply the baked sprite classification:

```java
@Override
protected PMeshRenderContext resolveMeshRender(RobotEntity entity,
                                               PBakedBone bone,
                                               PBakedMesh mesh,
                                               PMeshRenderContext inherited,
                                               float partialTick) {
    return inherited.withAlphaMode(PAlphaMode.AUTO);
}
```

Pass `OPAQUE`, `CUTOUT`, or `TRANSLUCENT` instead to force that mode. Calling `withAlphaMode(null)` returns control to the renderer's original `Function<ResourceLocation, RenderType>`.

Alpha-mode overrides select queued world render types. Do not set one in `resolveGuiMeshRender(...)` or another immediate-rendering context; use `trianglesGui` or `trianglesImmediate` there.

## Emissive textures

Emissive textures are useful for eyes, screens, lamps, energy parts, and other pieces that should ignore normal light. PulseLib reads this flag from texture metadata through [`PLibMetadata`](https://github.com/ArcAnc/PulseLib/blob/1.21.1/src/main/java/com/arcanc/pulselib/content/model/textures/atlas/PLibMetadata.java).

To mark a texture as emissive, add a `.png.mcmeta` file next to it:

```text
assets/examplemod/textures/entity/robot/eyes.png
assets/examplemod/textures/entity/robot/eyes.png.mcmeta
```

```json
{
  "pulselib": {
    "emissive": true
  }
}
```

When `PModelCache` bakes the model, each mesh stores whether its sprite is emissive. The default renderers automatically switch to an emissive variant through:

```java
PRenderTypes.RenderTypeProvider.emissiveVariant(baseType, PTextureCache.ATLAS_LOCATION);
```

You can also choose an emissive render type directly in custom rendering code:

```java
PRenderTypes.RenderTypeProvider::trianglesSolidEmissive
PRenderTypes.RenderTypeProvider::trianglesCutoutEmissive
PRenderTypes.RenderTypeProvider::trianglesTranslucentEmissive
PRenderTypes.RenderTypeProvider::trianglesGuiEmissive
PRenderTypes.RenderTypeProvider::trianglesImmediateEmissive
```

The queued emissive variants use the instanced emissive shader; GUI and immediate variants use the direct emissive shader. Emissive rendering applies texture color, tint, any configured alpha cutoff, and overlay but intentionally skips directional lighting and the lightmap. See [Shaders](shaders.md) for the full mapping.

## Per-mesh runtime material overrides

Renderers can replace a mesh texture at render time without duplicating the mesh in the source model. Override `resolveMeshRender(...)`, identify the target mesh by its UUID (or `textureName()`), and return a modified context:

```java
@Override
protected PMeshRenderContext resolveMeshRender(RobotEntity entity,
                                               PBakedBone bone,
                                               PBakedMesh mesh,
                                               PMeshRenderContext inherited,
                                               float partialTick) {
    if (!mesh.uuid().equals(ROBOT_SCREEN_MESH))
        return inherited;

    return inherited
            .withTexture(entity.activeScreenTexture())
            .withEmissive(entity.screenIsLit())
            .withAlphaMode(PAlphaMode.AUTO);
}
```

`withTexture(...)` accepts a texture location registered in the PulseLib runtime atlas. On its first use for a given `(mesh, texture)` pair, PulseLib bakes an alternate geometry variant with UVs mapped to that atlas sprite and classifies its alpha; later draws reuse that variant. Its GPU geometry and deformation caches are released on the next resource reload.

`withEmissive(true)` forces full-bright emissive rendering, `withEmissive(false)` disables it, and `withEmissive(null)` returns to the selected texture's `.mcmeta` setting. Alpha mode and emissive selection are independent: PulseLib first chooses the solid, cutout, or translucent base type, then switches it to the matching emissive variant when required.

Classes used:

* [`PTextureCache`](https://github.com/ArcAnc/PulseLib/blob/1.21.1/src/main/java/com/arcanc/pulselib/util/PTextureCache.java)
* [`RuntimeLoader`](https://github.com/ArcAnc/PulseLib/blob/1.21.1/src/main/java/com/arcanc/pulselib/content/model/textures/atlas/RuntimeLoader.java)
* [`PLibMetadata`](https://github.com/ArcAnc/PulseLib/blob/1.21.1/src/main/java/com/arcanc/pulselib/content/model/textures/atlas/PLibMetadata.java)
* [`PAlphaMode`](https://github.com/ArcAnc/PulseLib/blob/1.21.1/src/main/java/com/arcanc/pulselib/content/model/textures/PAlphaMode.java)
* [`PRenderTypes`](https://github.com/ArcAnc/PulseLib/blob/1.21.1/src/main/java/com/arcanc/pulselib/util/PRenderTypes.java)
