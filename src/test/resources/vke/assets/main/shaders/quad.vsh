#version 460

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec4 aColor;

layout (location = 0) out vec2 uv;

void main() {
    gl_Position = vec4(aPos, 1);
    uv = vec2((aPos.x + 1) / 2, (aPos.y + 1) / 2);
}