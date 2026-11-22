#version 450

#include("vke:assets/global/shaders/vke_sets.gdef")

layout(set = 2, binding = 0) uniform samplerCube u_Skybox;

layout(location = 0) in vec3 direction;
layout(location = 0) out vec4 outColor;

void main() {
    vec3 color = texture(u_Skybox, direction).rgb;
    outColor = vec4(color, 1.0);
}