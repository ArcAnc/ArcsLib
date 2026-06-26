#version 330

#moj_import <minecraft:light.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:sample_lightmap.glsl>

in vec3 Position;
in vec2 UV0;
in vec3 Normal;

struct InstanceDataStruct {
    mat4 InstanceModel;
    vec4 InstanceColor;
    vec2 InstanceLight;
    vec2 InstanceOverlay;
};

layout (std140) uniform InstanceData
{
    InstanceDataStruct data[512];
};

#ifndef NO_OVERLAY
uniform sampler2D Sampler1;
#endif

#ifndef EMISSIVE
uniform sampler2D Sampler2;
#endif

out vec4 vertexColorBack;
out vec4 vertexColorFront;

#ifndef EMISSIVE
out vec4 lightMapColor;
#endif

#ifndef NO_OVERLAY
out vec4 overlayColor;
#endif
out vec2 texCoord0;

void main()
{
    int id = gl_InstanceID;
    if (id >= 512)
    {
        gl_Position = vec4(0);
        return;
    }

    InstanceDataStruct inst = data[id];

    mat4 model = inst.InstanceModel;
    vec4 instanceColor = inst.InstanceColor;
    vec2 instanceLight = inst.InstanceLight;
    vec2 instanceOverlay = inst.InstanceOverlay;

    gl_Position = ProjMat * ModelViewMat * model * vec4(Position, 1.0);

    vec4 color = instanceColor;
    #ifdef EMISSIVE
    vertexColorBack = color;
    vertexColorFront = color;
    #else
    vec3 normalTransformed = normalize(mat3(model) * Normal);
    vec2 light = minecraft_compute_light(Light0_Direction, Light1_Direction, normalTransformed);
    vertexColorBack = minecraft_mix_light_separate(-light, color);
    vertexColorFront = minecraft_mix_light_separate(light, color);
    #endif

    #ifndef NO_OVERLAY
    overlayColor = texelFetch(Sampler1, ivec2(instanceOverlay), 0);
    #endif

    #ifndef EMISSIVE
    lightMapColor = sample_lightmap(Sampler2, ivec2(instanceLight*16));
    #endif

    texCoord0 = UV0;
}
