#version 450
#extension GL_EXT_buffer_reference : require

layout (location = 0) in vec3 inPos;
layout (location = 1) in vec3 inNormal;
layout (location = 2) in vec4 inColor;

layout (location = 0) out vec4 outColor;
layout (location = 1) out vec3 outNormal;

layout(push_constant) uniform constants {
    mat4 world;
    mat4 translation;
} PushConstants;

#MultipleWrites(100)
layout (set = 0, binding = 0) uniform Camera {
    mat4 proj;
    mat4 view;
} camera;

layout (set = 1, binding = 0) uniform sampler2D textures[];


void main() {
    gl_Position = PushConstants.world * PushConstants.translation * vec4(inPos, 1.0f);
    outColor = inColor;
    outNormal = inNormal;
}