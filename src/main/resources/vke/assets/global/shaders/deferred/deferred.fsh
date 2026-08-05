#version 450

layout (location = 0) in vec2 fUV;
layout (location = 1) in vec3 fNormal;
layout (location = 2) in flat int fMaterialId;

layout (location = 0) out vec4 gNormal;
layout (location = 1) out vec4 gAlbedoSpec;

#include("vke:assets/global/shaders/vke_sets.gdef")
#include("vke:assets/global/shaders/materials.gdef")

void main() {
    MaterialInputs m = evaluateMaterial(fMaterialId, fUV);
    gNormal = vec4(normalize(fNormal), 1.0);

    gAlbedoSpec.rgb = m.baseColor.rgb;

    gAlbedoSpec.a = m.specular.r;
}