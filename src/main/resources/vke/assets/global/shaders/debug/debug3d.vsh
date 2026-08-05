#version 450
layout (location = 0) in vec3 inPos;
layout (location = 1) in vec4 inColor;

layout (location = 0) out vec4 outColor;

#include("vke:assets/global/shaders/vke_sets.gdef")

void main() {
    gl_Position = frameData.camera.projection * frameData.camera.view * vec4(inPos, 1.0f);
    outColor = inColor;
}