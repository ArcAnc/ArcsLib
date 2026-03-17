#version 330

#moj_import <minecraft:light.glsl>

in vec3 Position;
in vec2 UV0;
in vec3 Normal;

layout(location = 4) in vec4 InstanceMatrix0;
layout(location = 5) in vec4 InstanceMatrix1;
layout(location = 6) in vec4 InstanceMatrix2;
layout(location = 7) in vec4 InstanceMatrix3;
layout(location = 8) in vec4 InstanceColor;
layout(location = 9) in vec2 InstanceLight;
layout(location = 10) in vec2 InstanceOverlay;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

uniform sampler2D Sampler1;
uniform sampler2D Sampler2;

uniform vec3 Light0_Direction;
uniform vec3 Light1_Direction;

out vec4 instanceColorOut;
out vec4 vertexColorBack;
out vec4 vertexColorFront;
out vec4 lightMapColor;
out vec4 overlayColor;
out vec2 texCoord0;

void main()
{
    mat4 InstanceMatrix = mat4(
    InstanceMatrix0,
    InstanceMatrix1,
    InstanceMatrix2,
    InstanceMatrix3
    );
    gl_Position = ProjMat * ModelViewMat * InstanceMatrix * vec4(Position, 1.0);

    vec3 normalTransformed = normalize(mat3(InstanceMatrix) * Normal);
    vec4 light = minecraft_mix_light(Light0_Direction, Light1_Direction, normalTransformed, Color);
    vertexColorBack = -light;
    vertexColorFront = light;

    overlayColor = texelFetch(Sampler1, ivec2(InstanceOverlay), 0);
    lightMapColor = texelFetch(Sampler2, ivec2(InstanceLight), 0);

    instanceColorOut = InstanceColor;
    texCoord0 = UV0;
}
