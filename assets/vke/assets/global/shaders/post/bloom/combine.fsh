#version 450

layout(location = 0) in vec2 uv;

layout (location = 0) out vec4 outColor;

#include("vke:assets/global/shaders/vke_sets.gdef")

layout (set = 2, binding = 0) uniform sampler2D u_Original;
layout (set = 2, binding = 1) uniform sampler2D u_Blurred;

void main() {
    vec3 sceneColor = vktexture(u_Original, uv).rgb;

    // 2. Sample 0.5x bloom texture (hardware bilinear sampler scales it to 1.0x automatically)
    vec3 bloomColor = vktexture(u_Blurred, uv).rgb;

    // 3. Additive blend
    outColor = vec4(sceneColor + (bloomColor * 0.04), 1);
}