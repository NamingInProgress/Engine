#version 450

layout (location = 0) in vec2 fUV;
layout (location = 1) in vec3 fNormal;
layout (location = 2) in flat ivec2 fTexIds;

layout (location = 0) out vec4 gNormal;
layout (location = 1) out vec4 gAlbedoSpec;

#include("vke:assets/global/shaders/vke_sets.gdef")

void main() {
    gNormal = vec4(normalize(fNormal), 1.0);

    gAlbedoSpec.rgb = sampleTexture(fTexIds.x, fUV).rgb;

    gAlbedoSpec.a = sampleTexture(fTexIds.y, fUV).r;
}