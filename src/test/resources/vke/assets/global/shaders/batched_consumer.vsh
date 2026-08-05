#version 450

layout (location = 0) in vec3 inPos;
layout (location = 1) in vec4 inColor;
layout (location = 2) in vec2 inUV;
layout (location = 3) in int texId;
layout (location = 4) in int matrixId;

layout (location = 0) out vec4 outColor;
layout (location = 1) out vec2 UV;
layout (location = 2) out flat int outTexId;

#include("vke:assets/global/shaders/vke_sets.gdef")

layout (std430, set = 2, binding = 0) readonly buffer MatrixStack {
    #DefaultSize(1024)
    mat4 modelMatrices[];
} matrixStack;

layout (push_constant) uniform constants {
    mat4 world;
} PushConstants;

void main() {
    gl_Position = PushConstants.world * matrixStack.modelMatrices[matrixId] * vec4(inPos, 1);
    outColor = inColor;
    UV = inUV;
    outTexId = texId;
}