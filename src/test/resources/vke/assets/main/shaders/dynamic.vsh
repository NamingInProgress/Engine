#version 450

layout (location = 0) in vec3 inPos;
layout (location = 1) in vec4 inColor;

layout (location = 0) out vec4 outColor;

layout (push_constant) uniform constants {
    mat4 world;
} PushConstants;

void main() {
    gl_Position = PushConstants.world * vec4(inPos, 1);
    outColor = inColor;
}