#ifndef PULSELIB_MATERIAL_FRAGMENT_GLSL
#define PULSELIB_MATERIAL_FRAGMENT_GLSL

vec4 pulselib_sample_material(sampler2D textureSampler, vec2 textureCoordinate) {
    vec4 color = texture(textureSampler, textureCoordinate);
#ifdef ALPHA_CUTOUT
    if (color.a < ALPHA_CUTOUT) {
        discard;
    }
#endif
    return color;
}

vec4 pulselib_lit_material(sampler2D textureSampler, vec2 textureCoordinate,
                           vec4 tint, vec4 backLight, vec4 frontLight,
                           vec4 overlay, vec4 lightMap) {
    vec4 color = pulselib_sample_material(textureSampler, textureCoordinate);
    color *= (gl_FrontFacing ? frontLight : backLight) * tint;
    color.rgb = mix(overlay.rgb, color.rgb, overlay.a);
    return color * lightMap;
}

vec4 pulselib_emissive_material(sampler2D textureSampler, vec2 textureCoordinate,
                                vec4 tint, vec4 overlay) {
    vec4 color = pulselib_sample_material(textureSampler, textureCoordinate);
    color *= tint;
    color.rgb = mix(overlay.rgb, color.rgb, overlay.a);
    return color;
}

#endif
