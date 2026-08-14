PulseLib does not draw model textures directly from arbitrary files. It first collects them into a runtime atlas, then the baked model stores UVs for that atlas. This is why texture registration is a required step instead of an optional convenience.

Texture registration uses [`PulseLibEvents.RegisterTextureEvent`](https://github.com/ArcAnc/PulseLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/event/PulseLibEvents.java).

## Register textures

Subscribe on the mod event bus and add every texture that a PulseLib model may use:

```java
@Mod.EventBusSubscriber(modid = ExampleMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ExampleClientEvents {
    @SubscribeEvent
    public static void registerPulseTextures(PulseLibEvents.RegisterTextureEvent event) {
        event.addTextureLocation(Identifier.fromNamespaceAndPath(
                ExampleMod.MOD_ID, "entity/robot/body"));
        event.addTextureLocation(Identifier.fromNamespaceAndPath(
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
Identifier.fromNamespaceAndPath("examplemod", "entity/robot/body")
```

## Runtime atlas

The atlas is registered by PulseLib itself. Your mod only contributes texture locations.

Runtime atlas classes:

* [`PTextureCache`](https://github.com/ArcAnc/PulseLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/PTextureCache.java)
* [`RuntimeLoader`](https://github.com/ArcAnc/PulseLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/model/textures/atlas/RuntimeLoader.java)

PulseLib registers the atlas at:

```java
PTextureCache.ATLAS_LOCATION // pulselib:textures/atlas.png
```

Renderers normally pass `PTextureCache.ATLAS_LOCATION` to `PRenderTypes`, so you rarely need to access the atlas manually.

## Per-mesh texture and emissive overrides

`PMeshRenderContext` can override a mesh's texture or emissive state at render time. The replacement texture must still be registered in the runtime atlas. The first use of a `(base mesh, replacement texture)` pair lazily bakes and caches a matching `PBakedMesh`; later renders reuse it.

```java
@Override
public PMeshRenderContext resolve(PBakedBone bone, PBakedMesh mesh,
                                  PMeshRenderContext inherited) {
    if (!mesh.textureName().equals("eyes"))
        return inherited;
    return inherited
            .withTexture(Identifier.fromNamespaceAndPath("examplemod", "entity/robot/eyes_active"))
            .withEmissive(true);
}
```

Pass `null` to `withTexture` or `withEmissive` to return to the mesh's baked texture or metadata-derived emissive value. An explicit `false` disables emissive even when the selected sprite metadata marks it emissive.

## Emissive textures

Emissive textures are useful for eyes, screens, lamps, energy parts, and other pieces that should ignore normal light. PulseLib reads this flag from texture metadata through [`PLibSpriteMetadata`](https://github.com/ArcAnc/PulseLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/model/textures/atlas/PLibSpriteMetadata.java).

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

When `PModelCache` bakes the model, each mesh stores whether its sprite is emissive. The default renderers also honour a `PMeshRenderContext.withEmissive(...)` override and automatically switch to an emissive variant through:

```java
PRenderTypes.RenderTypeProvider.emissiveVariant(baseType, PTextureCache.ATLAS_LOCATION);
```

You can also choose an emissive render type directly in custom rendering code:

```java
PRenderTypes.RenderTypeProvider::trianglesEmissiveCutout
PRenderTypes.RenderTypeProvider::trianglesEmissiveTranslucent
```

Choose the cutout or translucent variant according to the desired blend mode. There is no separate solid or GUI emissive render type in 26.1.

Classes used:

* [`PTextureCache`](https://github.com/ArcAnc/PulseLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/PTextureCache.java)
* [`RuntimeLoader`](https://github.com/ArcAnc/PulseLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/model/textures/atlas/RuntimeLoader.java)
* [`PLibSpriteMetadata`](https://github.com/ArcAnc/PulseLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/model/textures/atlas/PLibSpriteMetadata.java)
* [`PRenderTypes`](https://github.com/ArcAnc/PulseLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/PRenderTypes.java)
