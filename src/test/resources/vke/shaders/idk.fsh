#version 450

layout(location = 0) in vec4 col;

layout (location = 0) out vec4 color;

layout (set = 0, binding = 0) uniform Globals {
    mat4 matrix;
    float time;
} globals;

void main() {
    color = col * (1000.0 / globals.time);
}