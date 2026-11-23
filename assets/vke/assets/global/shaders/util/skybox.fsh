#version 450

#include("vke:assets/global/shaders/vke_sets.gdef")

layout(set = 2, binding = 0) uniform samplerCube u_Skybox;

layout(location = 0) in vec3 direction;
layout(location = 0) out vec4 outColor;

const float LIMIT = 65536.0;

float handleChannel(float value) {
    return isnan(value) ? LIMIT : (isinf(value) ? LIMIT : value);
}

void main() {
    vec3 color = texture(u_Skybox, direction).rgb;
    color.r = handleChannel(color.r);
    color.g = handleChannel(color.g);
    color.b = handleChannel(color.b);
    outColor = vec4(color, 1.0);
}