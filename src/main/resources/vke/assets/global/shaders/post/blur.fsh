#version 450

layout (location = 0) in vec2 outUV;

layout (location = 0) out vec4 FragColor;

#include("vke:assets/global/shaders/vke_sets.gdef")

layout (set = 3, binding = 0) uniform sampler2D u_ColorTex;

void main() {
    vec2 texel = 1.0 / vec2(textureSize(u_ColorTex, 0));

    vec4 sum = vec4(0.0);

    for (int x = -3; x <= 3; x++) {
        for (int y = -3; y <= 3; y++) {
            sum += texture(u_ColorTex, vec2(outUV.x, 1 - outUV.y) + vec2(x, y) * texel);
        }
    }

    FragColor = sum / 49.0;
}