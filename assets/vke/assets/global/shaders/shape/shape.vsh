#version 450

layout(location = 0) in vec3 pos;
layout(location = 1) in vec4 color;
layout(location = 2) in vec2 uv;
layout(location = 3) in int matId;
layout(location = 4) in int texId;

layout(location = 0) out vec4 outColor;
layout(location = 1) out vec2 outUv;
layout(location = 2) out int outTexId;

#include("vke:assets/global/shaders/vke_sets.gdef")

layout (std430, set = 2, binding = 0) readonly buffer Transforms {
    #DefaultSize(1024)
    mat4 transform[];
} transforms;

void main() {
    mat4 transform = transforms.transform[nonuniformEXT(matId)];

    gl_Position = transform * frameData.camera.projection * vec4(pos, 1.0);

    outColor = color;
    outUv = uv;
    outTexId = texId;
}