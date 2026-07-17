#version 450

#extension GL_EXT_nonuniform_qualifier : require

layout (location = 0) in vec4 inColor;
layout (location = 1) in vec2 UV;
layout (location = 2) flat in int texId;

layout (location = 0) out vec4 outColor;

#MultipleWrites(100)
layout (set = 0, binding = 0) uniform Camera {
    mat4 proj;
    mat4 view;
} camera;

layout (set = 1, binding = 0) uniform sampler2D textures[];

void main() {
    if (texId >= 0) {
        outColor = mix(texture(textures[nonuniformEXT(texId)], vec2(UV.x, 1. - UV.y)), inColor, 0.5);
    } else {
        outColor = inColor;
    }
}