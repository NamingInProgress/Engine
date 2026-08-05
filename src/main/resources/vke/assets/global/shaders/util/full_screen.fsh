#version 450

layout(location = 0) in vec2 uv;

layout (location = 0) out vec4 color;

#include("vke:assets/global/shaders/vke_sets.gdef")

layout (set = 2, binding = 0) uniform sampler2D u_InTex;

void main() {
    color = vktexture(u_InTex, uv);
}