#version 450

layout(location = 0) in vec4 col;

layout (location = 0) out vec4 color;

layout (set = 0, binding = 1) uniform Globals {
    float time;
} globals;

layout (set = 0, binding = 2) uniform Idk {
    float timev2;
} idks;

void main() {
    color = col * (1000.0 / idks.timev2);
}