This page is a compact map of PulseLib public API. Classes marked internal in source are listed only when they explain the public behavior.

## Animatable

* [`PAnimatable<T>`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/animatable/PAnimatable.java) - implemented by entities, block entities, items, and custom animatable objects.
* [`PItemAnimatable<T>`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/animatable/PItemAnimatable.java) - item animatable with `registerClientExtension`.
* [`PAnimationManager<T>`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/animatable/PAnimationManager.java) - owns controller factories, active controllers, bound model, and ticking.
* [`InstanceAnimationManager<T>`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/animatable/instance/InstanceAnimationManager.java) - manager for entity/block-entity style instances.
* [`SingletonAnimationManager<T>`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/animatable/singleton/SingletonAnimationManager.java) - keyed manager cache for singleton objects like items.
* [`AnimManagerKey`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/animatable/AnimManagerKey.java) - stable animation manager key from object, item stack, entity, or block entity.
* [`PAnimationController<T>`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/animatable/PAnimationController.java) - state handler, active animation, timing, stage progression, and bone frame sampling.
* [`ControllerState`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/animatable/ControllerState.java) - `PLAY`, `PAUSE`, `STOP`.
* [`PLibAnimationTicker`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/animatable/PLibAnimationTicker.java) - client ticking hook for animation managers.

## Animation model

* [`PRawAnimation`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/model/animation/PRawAnimation.java) - builder for runtime animation stage sequences.
* [`PAnimationType`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/model/animation/PAnimationType.java) - `PLAY_ONCE`, `HOLD_LAST_FRAME`, `CYCLE`.
* [`PAnimation`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/model/animation/PAnimation.java) - parsed animation with length, bone channels, and events.
* [`PBoneAnimation`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/model/animation/PBoneAnimation.java) - channels for one bone.
* [`PAnimationChannel`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/model/animation/PAnimationChannel.java) - `POSITION`, `ROTATION`, `SCALE`.
* [`PKeyFrameChannel`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/model/animation/PKeyFrameChannel.java) - position, rotation, and scale keyframes.
* [`PInterpolationType`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/model/animation/PInterpolationType.java) - interpolation functions and registry.
* [`BoneFrame`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/model/animation/BoneFrame.java) - sampled translation, rotation, scale.
* [`PAnimationEvent`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/model/animation/PAnimationEvent.java) - sealed sound/particle events.
* [`PAnimationEventDispatcher`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/animatable/PAnimationEventDispatcher.java) - client event dispatch and locator resolution.

## Model data and caches

* [`PModelData`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/modelData/PModelData.java) - renderer-facing model location, model format, and texture mapping.
* [`DefaultBlockModelData`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/modelData/DefaultBlockModelData.java)
* [`DefaultItemModelData`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/modelData/DefaultItemModelData.java)
* [`DefaultEntityModelData`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/modelData/DefaultEntityModelData.java)
* [`DefaultEntityLayerModelData`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/modelData/DefaultEntityLayerModelData.java)
* [`PModel`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/model/PModel.java) - raw loaded model.
* [`PBone`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/model/PBone.java) and [`PMesh`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/model/PMesh.java) - raw bones and meshes.
* [`PBakedModel`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/model/baked/PBakedModel.java), [`PBakedBone`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/model/baked/PBakedBone.java), [`PBakedMesh`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/model/baked/PBakedMesh.java) - baked GPU-ready model representation.
* [`PMeshRenderContext`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/model/baked/PMeshRenderContext.java) - render type, color, light, and overlay inherited or overridden per mesh.
* [`PMeshRenderResolver`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/model/baked/PMeshRenderResolver.java) - baked-bone/baked-mesh resolver used by immediate model drawing and attachment rendering.
* [`PModelCache`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/PModelCache.java) - model loader registry, client reload listener, baking, cache cleanup.

## Loading

* [`PModelLoader`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/data/PModelLoader.java) - model loader interface.
* [`PGltfModelLoader`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/data/PGltfModelLoader.java) and [`PGltfModelParser`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/data/PGltfModelParser.java)
* [`PGeckoModelLoader`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/data/PGeckoModelLoader.java) and [`PGeckoModelParser`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/data/PGeckoModelParser.java)
* [`PGltfAnimationEventSidecarParser`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/data/PGltfAnimationEventSidecarParser.java)
* [`PGeckoAnimationEventParser`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/data/PGeckoAnimationEventParser.java)

## Rendering

* [`PRenderer<T, RS>`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/PRenderer.java) - render-state based renderer contract.
* [`PBlockRenderer<T, RS>`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/PBlockRenderer.java)
* [`PItemRenderer<T, RS>`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/PItemRenderer.java)
* [`PEntityRenderer<T, RS>`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/PEntityRenderer.java)
* [`PEntityRenderLayer<T, RS>`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/PEntityRenderLayer.java)
* [`PRenderState<T>`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/base/PRenderState.java), [`PBlockRenderState<T>`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/base/PBlockRenderState.java), [`PItemRenderState<T>`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/base/PItemRenderState.java), [`PEntityRenderState<T>`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/base/PEntityRenderState.java) - renderer state extraction and cached model/animatable/key access.
* [`PRenderTypes`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/PRenderTypes.java) - triangle render types, shaders, vertex format.
* [`PRenderQueue`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/renderer/PRenderQueue.java) - instanced render submission.

## Textures

* [`PTextureCache`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/PTextureCache.java) - runtime atlas registration and texture cache.
* [`RuntimeLoader`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/model/textures/atlas/RuntimeLoader.java) - atlas sprite source.
* [`PLibSpriteMetadata`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/model/textures/atlas/PLibSpriteMetadata.java) - `pulselib.emissive` texture metadata.
* [`PulseLibEvents.RegisterTextureEvent`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/event/PulseLibEvents.java) - mod bus texture registration event.

## Armor and attachments

* [`PAttachmentAnchor`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/attachments/PAttachmentAnchor.java)
* [`PAttachmentAnchorResolvers`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/attachments/PAttachmentAnchorResolvers.java)
* [`PAttachmentBinding`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/attachments/PAttachmentBinding.java)
* [`PTransform`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/attachments/PTransform.java)
* [`PLivingAttachmentSource`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/attachments/PLivingAttachmentSource.java)
* [`PLivingAttachmentSources`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/attachments/PLivingAttachmentSources.java)
* [`PLivingAttachmentDefinition`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/attachments/PLivingAttachmentDefinition.java)
* [`PLivingAttachments`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/attachments/PLivingAttachments.java)
* [`PLivingMeshRenderResolver`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/attachments/PLivingMeshRenderResolver.java)
* [`PLivingMeshRenderResolvers`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/attachments/PLivingMeshRenderResolvers.java)
* [`PHumanoidAnchors`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/attachments/humanoid/PHumanoidAnchors.java)
* [`PHumanoidBindings`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/attachments/humanoid/PHumanoidBindings.java)
* [`PLivingAttachmentLayer`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/attachments/PLivingAttachmentLayer.java)
* [`PHumanoidAttachmentLayer`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/attachments/humanoid/PHumanoidAttachmentLayer.java)
* [`PArmorClientExtensions`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/attachments/humanoid/armor/PArmorClientExtensions.java)
* [`PLibArmorHandler`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/attachments/humanoid/armor/PLibArmorHandler.java)
* [`PulseLibEvents.AttachmentRegistrationEvent`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/event/PulseLibEvents.java) - mod bus attachment definition registration event.

## Player animations

* [`PPlayerAnimationDefinition`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/player/animation/PPlayerAnimationDefinition.java) - client-side definition, bindings, mask, controller registration, blend mode, and weight.
* [`PPlayerAnimations`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/player/animation/PPlayerAnimations.java) - player-animation registry and internal pose application.
* [`PPlayerAnimationInstance`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/player/animation/PPlayerAnimationInstance.java) - per-player, per-definition controller state.
* [`PPlayerAnimationHandle`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/player/animation/PPlayerAnimationHandle.java) - public runtime control of named player-animation controllers.
* [`PPlayerAnimationMask`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/player/animation/PPlayerAnimationMask.java) - static or dynamic per-part animation mask.
* [`PPlayerPart`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/player/animation/PPlayerPart.java) - semantic vanilla player parts, including outer skin layers.
* [`PPlayerAnimationBlendMode`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/player/animation/PPlayerAnimationBlendMode.java) - `ADDITIVE` or `REPLACE`.
* [`PPlayerAnimationWeight`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/player/animation/PPlayerAnimationWeight.java) - constant or dynamic render-time blend weight.
* [`PulseLibEvents.PlayerAnimationRegistrationEvent`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/content/event/PulseLibEvents.java) - client mod-bus registration event.

## Helpers

* [`PLibHelper`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/helpers/PLibHelper.java) - manager creation and default entity/living render-state factories.
* [`PLibRenderHelper`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/helpers/PLibRenderHelper.java) - `Minecraft.getInstance()` helper.
* [`PLibParserHelper`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/helpers/PLibParserHelper.java) - glTF accessor helpers.
* [`PLibDatabase`](https://github.com/ArcAnc/ArcsLib/blob/26.1/src/main/java/com/arcanc/pulselib/util/PLibDatabase.java) - constants and `rl(String)`.
