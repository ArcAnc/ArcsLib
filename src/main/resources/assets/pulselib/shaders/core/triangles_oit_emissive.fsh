#version 330

#moj_import <pulselib:material_fragment.glsl>
#moj_import <pulselib:weighted_oit.glsl>

uniform sampler2D Sampler0;

in vec4 instanceColorOut;
in vec4 overlayColor;
in vec2 texCoord0;

layout(location = 0) out vec4 accumulation;
layout(location = 1) out float revealage;

void main()
{
    vec4 color = pulselib_emissive_material(Sampler0, texCoord0, instanceColorOut, overlayColor);
    pulselib_write_weighted_oit(color, gl_FragCoord.z, accumulation, revealage);
}
