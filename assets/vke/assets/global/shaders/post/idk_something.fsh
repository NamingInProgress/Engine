#version 450

layout (location = 0) in vec2 outUV;

layout (location = 0) out vec4 FragColor;

#include("vke:assets/global/shaders/vke_sets.gdef")

layout (set = 2, binding = 0) uniform sampler2D u_ColorTex;

void main() {
    vec2 uv = outUV;

    float wave = sin(uv.y * 20.0 + frameData.time * 3.0) + sin(uv.x * 15.0 - frameData.time * 2.0);

    uv += 0.015 * vec2(
        sin(wave + frameData.time),
        cos(wave + frameData.time)
    );

    vec4 color = texture(u_ColorTex, vec2(uv.x, 1 - uv.y));

    float pulse = 0.9 + 0.1 * sin(frameData.time * 2.0 + wave);

    color.rgb *= pulse;

    FragColor = color;
}