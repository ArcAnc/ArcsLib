* fixed glTF meshes without a parent bone being discarded by creating a local render bone for them
* fixed glTF animation rotations being applied outside their local bone space
* added automatic opaque, cutout, and translucent texture classification
* added weighted blended order-independent transparency for queued translucent meshes
* fixed mesh render resolvers losing inherited texture, emissive, alpha-mode, and deformation overrides
* fixed withEmissive(false) retaining full-bright lighting from emissive texture metadata
* finally fixed OIT, also added fix to first person renderer
* fixed huge fps drop with active OIT