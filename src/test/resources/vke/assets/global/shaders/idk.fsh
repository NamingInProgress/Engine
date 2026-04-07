#version 450

layout(location = 0) in vec4 col;
layout(location = 1) in vec2 uv;

layout (location = 0) out vec4 color;

layout (set = 0, binding = 0) uniform sampler2D tex;

void main() {
    //color = col * (abs(sin((globals.time / 500) - 1))) * data.customColor;
    //color = col;
    //color = mix(col, texture(tex, uv), 0.5);
    color = texture(tex, vec2(uv.x, 1.0 - uv.y));
}