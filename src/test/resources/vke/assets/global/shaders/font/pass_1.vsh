#version 450

layout (location = 0) in vec3 inPos;
layout (location = 1) in int matrixId;

#include("vke:assets/global/shaders/vke_sets.gdef")

layout (std430, set = 2, binding = 0) readonly buffer MatrixStack {
#DefaultSize(1024)
    mat4 modelMatrices[];
} u_MatrixStack;

layout (push_constant) uniform constants {
    mat4 projection;
} PushConstants;

void main() {
    gl_Position = PushConstants.projection * u_MatrixStack.modelMatrices[matrixId] * vec4(inPos, 1);
}