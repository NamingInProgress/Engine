#version 450

layout(location = 0) in vec4 col;

layout (location = 0) out vec4 color;

#include("vke:assets/global/shaders/vke_sets.gdef")

void main() {
    color = col;
}