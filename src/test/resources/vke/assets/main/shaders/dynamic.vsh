#version 450

layout (location = 0) in vec3 inPos;
layout (location = 1) in vec4 inColor;

layout (location = 0) out vec4 outColor;

#MultipleWrites(100)
layout (set = 0, binding = 0) uniform Camera {
    mat4 proj;
    mat4 view;
} camera;

layout (set = 1, binding = 0) uniform sampler2D textures[];


layout (push_constant) uniform constants {
    mat4 world;
    mat4 translation;
} PushConstants;

void main() {
    gl_Position = PushConstants.world * PushConstants.translation * vec4(inPos, 1);
    outColor = inColor;
}