#version 450

layout (location = 0) in vec4 inColor;
layout (location = 1) in vec2 UV;
layout (location = 2) flat in int texId;

layout (location = 0) out vec4 outColor;

layout (set = 0, binding = 0) uniform sampler2D[2] textures;

void main() {
    if (texId >= 0) {
        outColor = mix(texture(textures[texId], UV), inColor, 0.5);
    } else {
        outColor = inColor;
    }
}