#version 330

#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;
uniform sampler2D LayerDepthSampler;

in vec2 texCoord0;

layout(location = 0) out float ignoredColor;

const float PULSELIB_OIT_EPSILON = 0.000001;

void main()
{
    vec4 color = texture(Sampler0, texCoord0);
    #ifdef ALPHA_CUTOUT
    if (color.a < ALPHA_CUTOUT) {
        discard;
    }
    #endif

    if (color.a * ColorModulator.a <= PULSELIB_OIT_EPSILON) {
        discard;
    }

    float previousLayerDepth = texelFetch(LayerDepthSampler, ivec2(gl_FragCoord.xy), 0).r;
    if (gl_FragCoord.z >= previousLayerDepth - PULSELIB_OIT_EPSILON) {
        discard;
    }

    ignoredColor = 0.0;
}
