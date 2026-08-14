#version 330

#moj_import <minecraft:light.glsl>
#moj_import <pulselib:deformers.glsl>

in vec3 Position;
in vec2 UV0;
in vec3 Normal;

layout(location = 4) in vec4 InstanceRow0;
layout(location = 5) in vec4 InstanceRow1;
layout(location = 6) in vec4 InstanceRow2;
layout(location = 7) in vec4 InstanceColor;
layout(location = 8) in vec2 InstanceLight;
layout(location = 9) in vec2 InstanceOverlay;
layout(location = 10) in ivec3 InstanceDeformer;

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
    vec4(InstanceRow0.x, InstanceRow1.x, InstanceRow2.x, 0.0),
    vec4(InstanceRow0.y, InstanceRow1.y, InstanceRow2.y, 0.0),
    vec4(InstanceRow0.z, InstanceRow1.z, InstanceRow2.z, 0.0),
    vec4(InstanceRow0.w, InstanceRow1.w, InstanceRow2.w, 1.0)
    );
    vec3 deformedPosition;
    vec3 deformedNormal;
    pulselib_deform_vertex(Position, Normal, InstanceDeformer, deformedPosition, deformedNormal);
    gl_Position = ProjMat * ModelViewMat * InstanceMatrix * vec4(deformedPosition, 1.0);

    vec3 normalTransformed = normalize(transpose(inverse(mat3(InstanceMatrix))) * deformedNormal);
    vertexColorBack = -minecraft_mix_light(Light0_Direction, Light1_Direction, -normalTransformed, InstanceColor);
    vertexColorFront = minecraft_mix_light(Light0_Direction, Light1_Direction, normalTransformed, InstanceColor);

    overlayColor = texelFetch(Sampler1, ivec2(InstanceOverlay), 0);
    lightMapColor = texelFetch(Sampler2, ivec2(InstanceLight), 0);

    instanceColorOut = InstanceColor;
    texCoord0 = UV0;
}
