* upgraded translucent PulseLib meshes to four-layer depth-peeled weighted OIT, improving overlapping transparent surfaces
* render entity and translucent block submissions through one shared transparency pass so OIT sees both sets of meshes together
* render first-person PulseLib items in a dedicated stage and composite their transparency immediately after hand rendering
* added dedicated OIT depth and depth-peel shader programs for lit and emissive materials
* rewrote shader fractional constants in ordinary decimal notation instead of scientific notation
* fixed PulseLib meshes overwriting each other in shared geometry buffers, which could make models disappear when multiple custom meshes were visible
* fixed persistent MultiDrawIndirect command ranges being reused by OIT passes in the same frame, restoring item, armor, transparency, and depth-sorting rendering