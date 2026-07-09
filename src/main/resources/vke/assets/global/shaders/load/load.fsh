#version 450

layout(location = 0) in vec4 col;

layout (location = 0) out vec4 color;

#MultipleWrites(100)
layout (set = 0, binding = 0) uniform Camera {
    mat4 proj;
    mat4 view;
} camera;

layout (set = 1, binding = 0) uniform sampler2D textures[];

void main() {
    color = col;
}