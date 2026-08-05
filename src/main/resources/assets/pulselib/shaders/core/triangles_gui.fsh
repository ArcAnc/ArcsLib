#version 330

#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

<<<<<<< HEAD
=======
in vec4 vertexColorBack;
in vec4 vertexColorFront;
in vec4 lightMapColor;
>>>>>>> e194067 (Tons of e)
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
<<<<<<< HEAD
    color *= ColorModulator;
=======
    color *= gl_FrontFacing ? vertexColorFront : vertexColorBack;
>>>>>>> e194067 (Tons of e)
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    fragColor = color;
}
