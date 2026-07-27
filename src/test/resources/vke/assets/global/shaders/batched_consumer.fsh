#version 450

layout (location = 0) in vec4 inColor;
layout (location = 1) in vec2 UV;
layout (location = 2) flat in int texId;

layout (location = 0) out vec4 outColor;

#include("vke:assets/global/shaders/vke_sets.gdef")

void main() {
    if (texId >= 0) {
        outColor = mix(texture(textures[nonuniformEXT(texId)], vec2(UV.x, 1. - UV.y)), inColor, 0.5);
    } else {
        outColor = inColor;
    }
}