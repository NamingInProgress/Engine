#version 450
layout (location = 0) in vec2 inPos;
layout (location = 1) in vec4 inColor;

layout (location = 0) out vec4 outColor;

#include("vke:assets/global/shaders/vke_sets.gdef")

void main() {
    gl_Position = vec4(inPos, 0.0, 1.0f);
    outColor = inColor;
}