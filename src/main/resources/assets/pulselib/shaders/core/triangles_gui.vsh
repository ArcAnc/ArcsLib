#version 150

#moj_import <minecraft:light.glsl>

in vec3 Position;
in vec2 UV0;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

uniform vec4 Color;
uniform ivec2 Light;
uniform ivec2 Overlay;

uniform sampler2D Sampler1;
uniform sampler2D Sampler2;

uniform vec3 Light0_Direction;
uniform vec3 Light1_Direction;

out vec4 instanceColor;
out vec4 lightMapColor;
out vec4 overlayColor;
out vec2 texCoord0;

void main()
{
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    overlayColor = texelFetch(Sampler1, Overlay, 0);
    lightMapColor = texelFetch(Sampler2, Light, 0);

    instanceColor = Color;
    texCoord0 = UV0;
}
