#version 450
layout (location = 0) in vec2 inPos;
layout (location = 1) in vec4 inColor;

layout (location = 0) out vec4 outColor;

layout (set = 1, binding = 0) uniform sampler2D textures[];

#MultipleWrites(100)
layout (set = 0, binding = 0) uniform Camera {
    mat4 proj;
    mat4 view;
} camera;

void main() {
    gl_Position = vec4(inPos, 0.0, 1.0f);
    outColor = inColor;
}