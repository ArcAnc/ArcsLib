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
```

`trianglesLitEmissive` remains only as the compatibility counterpart of `trianglesLit`; it is not a distinct shader variant for new rendering code.

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
            .withEmissive(entity.screenIsLit());
}
```

`withTexture(...)` accepts a texture location registered in the PulseLib runtime atlas. On its first use for a given `(mesh, texture)` pair, PulseLib bakes an alternate vertex buffer with UVs mapped to that atlas sprite; later draws reuse that buffer. The variant buffers and their deformation caches are released on the next resource reload.

`withEmissive(true)` forces full-bright emissive rendering, `withEmissive(false)` disables it, and `withEmissive(null)` returns to the selected texture's `.mcmeta` setting.

Classes used:

* [`PTextureCache`](https://github.com/ArcAnc/PulseLib/blob/1.21.1/src/main/java/com/arcanc/pulselib/util/PTextureCache.java)
* [`RuntimeLoader`](https://github.com/ArcAnc/PulseLib/blob/1.21.1/src/main/java/com/arcanc/pulselib/content/model/textures/atlas/RuntimeLoader.java)
* [`PLibMetadata`](https://github.com/ArcAnc/PulseLib/blob/1.21.1/src/main/java/com/arcanc/pulselib/content/model/textures/atlas/PLibMetadata.java)
* [`PRenderTypes`](https://github.com/ArcAnc/PulseLib/blob/1.21.1/src/main/java/com/arcanc/pulselib/util/PRenderTypes.java)
