#version 330

#moj_import <minecraft:light.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:sample_lightmap.glsl>

in vec3 Position;
in vec2 UV0;
in vec3 Normal;

<<<<<<< HEAD
uniform sampler2D Sampler0;
=======
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat3 NormalMat;

uniform vec4 Color;
uniform ivec2 Light;
uniform ivec2 Overlay;

>>>>>>> e194067 (Tons of e)
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;

layout(std140) uniform ColorOverlay {
    vec4 Color;
    ivec2 Overlay;
};

<<<<<<< HEAD
=======
out vec4 vertexColorBack;
out vec4 vertexColorFront;
out vec4 lightMapColor;
>>>>>>> e194067 (Tons of e)
out vec4 overlayColor;
out vec2 texCoord0;

void main()
{
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vec3 normalTransformed = normalize(NormalMat * Normal);
    vertexColorFront = minecraft_mix_light(Light0_Direction, Light1_Direction, normalTransformed, Color);
    vertexColorBack = minecraft_mix_light(Light0_Direction, Light1_Direction, -normalTransformed, Color);

    overlayColor = texelFetch(Sampler1, Overlay, 0);

    texCoord0 = UV0;
}