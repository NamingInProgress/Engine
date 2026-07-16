#version 450

layout (location = 0) in vec2 outUV;

layout (location = 0) out vec4 FragColor;

#include("vke:assets/global/shaders/vke_sets.gdef")

layout (set = 3, binding = 0) uniform sampler2D u_ColorTex;

void main() {
    vec4 temp = texture(u_ColorTex, vec2(outUV.x, 1 - outUV.y));
    FragColor = vec4(1 - temp.r, 1 - temp.g, 1 - temp.b, temp.a);
}