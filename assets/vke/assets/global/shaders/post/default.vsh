#version 450

layout (location = 0) in vec2 pos;
layout (location = 1) in vec2 uv;

layout (location = 0) out vec2 outUV;

#include("vke:assets/global/shaders/vke_sets.gdef")

void main() {
    gl_Position = vec4(pos, 0, 1);
    outUV = uv;
}