#version 450

layout (location = 0) in vec2 fUV;
layout (location = 1) in vec3 fNormal;
layout (location = 2) in flat int fMaterialId;
layout (location = 3) in vec4 fTangent;

layout (location = 0) out vec4 gNormal;
layout (location = 1) out int gMaterialIdx;
layout (location = 2) out vec2 gMeshUvs;

#include("vke:assets/global/shaders/vke_sets.gdef")
#include("vke:assets/global/shaders/materials.gdef")

void main() {
    gNormal = vec4(fNormal, 1.0);
    if (fMaterialId == NO_MATERIAL) {
        gNormal = vec4(fNormal, 1.0);
    } else {
        Material m = u_MaterialBuffer.materials[fMaterialId];
        vec3 N = normalize(fNormal);
        vec3 T = normalize(fTangent.xyz - N * dot(N, fTangent.xyz));
        vec3 B = cross(N, T) * fTangent.w;
        mat3 TBN = mat3(T, B, N);

        //vec3 tangentNormal = sampleTexture(m.normalId, fUV).xyz * 2.0 - 1.0;
        vec3 tangentNormal = vec3(0.0, 0.0, 1.0);
        gNormal = vec4(normalize(TBN * tangentNormal), 1.0);
    }

    //gNormal = vec4(fNormal, 1.0);
    gMaterialIdx = fMaterialId;
    gMeshUvs = fUV;
}