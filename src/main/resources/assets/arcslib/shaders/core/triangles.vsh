#version 330

#moj_import <minecraft:light.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec2 UV0;
in vec3 Normal;

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;

out vec4 vertexColorBack;
out vec4 vertexColorFront;
out vec4 lightMapColor;
out vec4 overlayColor;
out vec2 texCoord0;

layout(std140) uniform ColorLightOverlay {
    vec4 Color;
    ivec2 Light;
    ivec2 Overlay;
};

void main()
{
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vec2 light = minecraft_compute_light(Light0_Direction, Light1_Direction, Normal);
    vertexColorBack = minecraft_mix_light_separate(-light, Color);
    vertexColorFront = minecraft_mix_light_separate(light, Color);

    overlayColor = texelFetch(Sampler1, Overlay, 0);
    lightMapColor = texelFetch(Sampler2, Light / 16, 0);

    texCoord0 = UV0;
}
