#version 450

layout(location = 0) in vec4 col;

layout (location = 0) out vec4 color;

layout (set = 0, binding = 0) uniform Globals {
    mat4 matrix;
    float time;
} globals;

layout (set = 0, binding = 1, std430) readonly buffer DataBuffer {
    vec4 customColor;
} data;

void main() {
    color = col * (abs(sin((globals.time / 500) - 1))) * data.customColor;
    color = vec4(color.rgb, 1);
}