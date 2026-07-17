#version 450

layout (location = 0) in vec3 inPos;
layout (location = 1) in vec3 inNormal;
layout (location = 2) in vec4 inColor;

layout (location = 0) out vec4 outColor;
layout (location = 1) out vec3 outNormal;

#include("vke:assets/global/shaders/vke_sets.gdef")

#MultipleWrites(2)
layout(std430, set = 2, binding = 0) readonly buffer SSBO {
    #DefaultSize(1024)
    mat4 matrices[];
} ssbo;

layout(push_constant) uniform constants {
    mat4 projection;
} PushConstants;

void main() {
    gl_Position = PushConstants.projection * ssbo.matrices[gl_InstanceIndex] * vec4(inPos, 1.0f);
    outColor = inColor;
    outNormal = inNormal;
}