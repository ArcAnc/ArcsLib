PulseLib supports sound and particle events inside animations.

Public event classes:

* [`PAnimationEvent`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/content/model/animation/PAnimationEvent.java)
* [`PAnimationEventDispatcher`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/content/animatable/PAnimationEventDispatcher.java)
* [`PGltfAnimationEventSidecarParser`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/data/PGltfAnimationEventSidecarParser.java)
* [`PGeckoAnimationEventParser`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/data/PGeckoAnimationEventParser.java)

Events are stored in [`PAnimation`](https://github.com/ArcAnc/PulseLib/blob/master/src/main/java/com/arcanc/pulselib/content/model/animation/PAnimation.java). A controller fires all events between the previous and current animation time while the controller is ticking.

## glTF sidecar files

For `.glb` and `.gltf` models, PulseLib looks for sidecar files in these locations:

```text
glmodels/<model>.events.json
glmodels/<model>.animation_events.json
glmodels/events/<model>.events.json
glmodels/events/<fileName>.events.json
```

Example:

```json
{
  "animations": {
    "attack": {
      "events": [
        {
          "type": "sound",
          "time": 0.25,
          "sound": "minecraft:entity.player.attack.strong",
          "locator": "hand_right",
          "volume": 1.0,
          "pitch": 1.0
        },
        {
          "type": "particle",
          "time": 0.35,
          "particle": "minecraft:crit",
          "locator": "blade_tip",
          "offset": [0.0, 0.0, 0.0],
          "motion": [0.0, 0.02, 0.0]
        }
      ]
    }
  }
}
```

`time` is written in seconds in the sidecar and converted to ticks internally with `seconds * 20`.

## Gecko animation events

The Gecko parser reads `sound_effects` and `particle_effects` from animation JSON. Time keys are seconds and are also converted to ticks.

```json
{
  "animations": {
    "animation.example.attack": {
      "sound_effects": {
        "0.25": {
          "sound": "minecraft:entity.player.attack.strong",
          "locator": "hand_right"
        }
      },
      "particle_effects": {
        "0.35": {
          "particle": "minecraft:crit",
          "locator": "blade_tip",
          "offset": [0, 0, 0],
          "motion": [0, 0.02, 0]
        }
      }
    }
  }
}
```

## Locator behavior

If an event has a non-empty `locator`, `PAnimationEventDispatcher` tries to resolve a bone with that name using the current model and controllers. If it succeeds, the sound or particle is spawned at that animated bone position. If it fails, the event falls back to the animatable's base position.

Supported base positions:

* `Entity`: entity coordinates, rotated by body yaw for locator offsets.
* `BlockEntity`: block center, rotated from `HORIZONTAL_FACING` or `FACING` when present.

Dispatch only runs on the client side.

Manual dispatch:

```java
PAnimationEvent event = new PAnimationEvent.Sound(
        0,
        Identifier.withDefaultNamespace("block.note_block.pling"),
        "",
        1.0f,
        1.0f);

PAnimationEventDispatcher.dispatch(animatable, event);
```
