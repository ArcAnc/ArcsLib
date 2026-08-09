#version 330

#moj_import <minecraft:light.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:sample_lightmap.glsl>

in vec3 Position;
in vec2 UV0;
in vec3 Normal;

layout(std140) uniform ColorOverlay {
    vec4 Color;
    vec2 Light;
    ivec2 Overlay;
};

uniform sampler2D Sampler1;
uniform sampler2D Sampler2;

out vec4 vertexColorBack;
out vec4 vertexColorFront;
out vec4 lightMapColor;
out vec4 overlayColor;
out vec2 texCoord0;

void main()
{
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vec3 normalTransformed = normalize(mat3(ModelViewMat) * Normal);
    vertexColorFront = minecraft_mix_light(Light0_Direction, Light1_Direction, normalTransformed, Color);
    vertexColorBack = minecraft_mix_light(Light0_Direction, Light1_Direction, -normalTransformed, Color);

    overlayColor = texelFetch(Sampler1, Overlay, 0);
    lightMapColor = sample_lightmap(Sampler2, ivec2(Light * 16));

    texCoord0 = UV0;
}
