[`PModelData`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/modelData/PModelData.java) is the small object that tells a renderer, "this is the model file, and these are the textures the model should use."

It is deliberately separate from the renderer. That lets the same renderer logic stay simple while different blocks, items, entities, or layers point at different files. If a PulseLib model is invisible or has missing textures, `PModelData` is one of the first things to inspect.

## Direct builder

Use the direct builder when you already know the exact model path, or when you are using a folder layout that does not match PulseLib's default conventions.

```java
PModelData data = new PModelData.Builder(
        Identifier.fromNamespaceAndPath("examplemod", "glmodels/block/crusher.glb"),
        "")
        .addTexture("body", Identifier.fromNamespaceAndPath("examplemod", "block/crusher/body"))
        .addTexture("glass", Identifier.fromNamespaceAndPath("examplemod", "block/crusher/glass"))
        .build();
```

Passing an empty `modelType` means "do not rewrite this path." Passing a non-empty type lets PulseLib ask the active model loader to build the conventional path.

## Default builders

For normal mods, default builders are more pleasant because they keep model and texture layout consistent. You give them the short logical name, and they expand it into the resource-pack path.

```java
PModelData blockData = new DefaultBlockModelData.DefaultBlockModelDataBuilder(
        Identifier.fromNamespaceAndPath("examplemod", "crusher"))
        .addTexture(Identifier.fromNamespaceAndPath("examplemod", "body"))
        .addTexture(Identifier.fromNamespaceAndPath("examplemod", "glass"))
        .build();
```

For the default glTF loader, the example resolves to:

```text
Model:    assets/examplemod/glmodels/block/crusher.glb
Textures: assets/examplemod/textures/block/crusher/body.png
          assets/examplemod/textures/block/crusher/glass.png
```

Available default builders:

* [`DefaultBlockModelData`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/modelData/DefaultBlockModelData.java)
* [`DefaultItemModelData`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/modelData/DefaultItemModelData.java)
* [`DefaultEntityModelData`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/modelData/DefaultEntityModelData.java)
* [`DefaultEntityLayerModelData`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/modelData/DefaultEntityLayerModelData.java)

## Texture name resolution

`PModelData.getTextureByName(name)` first checks explicit mappings added with `addTexture`. If a texture was not registered explicitly, PulseLib asks [`PModelCache.resolveTextureLocation`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/PModelCache.java) to infer a path from the model path and texture name.

That fallback is useful, but explicit names are easier to debug. Keep Blockbench/glTF texture names aligned with the names you add through `PModelData`, especially on models with several materials.

## Gecko model data

PulseLib can load Gecko-style JSON models through [`PGeckoModelLoader`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/data/gecko/PGeckoModelLoader.java), but the default registered loader in `PModelCache` is glTF. Register the Gecko loader before client resources reload if your mod needs Gecko paths:

```java
PModelCache.registerModelLoader(PGeckoModelLoader.INSTANCE);

PModelData data = new DefaultEntityModelData.DefaultEntityModelDataBuilder(
        Identifier.fromNamespaceAndPath("examplemod", "robot"),
        PGeckoModelLoader.INSTANCE.id())
        .build();
```

Classes used:

* [`PModelData`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/modelData/PModelData.java)
* [`PModelCache`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/PModelCache.java)
* [`PGltfModelLoader`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/data/gltf/PGltfModelLoader.java)
* [`PGeckoModelLoader`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/data/gecko/PGeckoModelLoader.java)
