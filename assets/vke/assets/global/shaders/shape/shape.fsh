#version 450

#extension GL_EXT_nonuniform_qualifier : require

layout(location = 0) in vec4 color;
layout(location = 1) in vec2 uv;
layout(location = 2) in flat int texId;

layout(location = 0) out vec4 outColor;

#include("vke:assets/global/shaders/vke_sets.gdef")

void main() {
    if (texId < 0) {
        outColor = color;
    } else {
        outColor = mix(sampleTexture(texId, uv), color, color.a);
    }
}