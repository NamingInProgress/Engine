#version 450

layout (location = 0) in vec4 inColor;

layout (location = 0) out vec4 outColor;

#include("vke:assets/global/shaders/vke_sets.gdef")

void main() {
    outColor = inColor;
}