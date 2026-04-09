#version 450
#extension GL_EXT_buffer_reference : require

layout (location = 0) in vec3 inPos;
layout (location = 1) in vec4 inColor;

layout (location = 0) flat out vec4 outColor;

layout(push_constant) uniform constants {
    mat4 world;
    mat4 translation;
} PushConstants;

void main() {
    gl_Position = PushConstants.world * PushConstants.translation * vec4(inPos, 1.0f);
    outColor = inColor;
}