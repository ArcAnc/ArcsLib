#version 330

uniform sampler2D Sampler0;

in vec4 instanceColorOut;
in vec4 overlayColor;
in vec2 texCoord0;

out vec4 fragColor;

void main()
{
    vec4 color = texture(Sampler0, texCoord0);
    #ifdef ALPHA_CUTOUT
    if (color.a < ALPHA_CUTOUT) {
        discard;
    }
    #endif
    color *= instanceColorOut;
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    fragColor = color;
}
