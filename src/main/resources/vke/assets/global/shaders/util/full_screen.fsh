#version 450

layout(location = 0) in vec2 uv;

layout (location = 0) out vec4 color;

#include("vke:assets/global/shaders/vke_sets.gdef")

layout (set = 2, binding = 0) uniform sampler2D u_InTex;

void main() {
    vec4 c = vktexture(u_InTex, uv);

    //color = vec4(c.xyz + vec3(0.5), c.a);
    color = c;
}