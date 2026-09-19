#version 450

layout (location = 0) in vec3 inPos;
layout (location = 1) in vec3 inNormal;
layout (location = 2) in vec2 inUV;
layout (location = 3) in vec4 inTangent;

layout (location = 0) out vec2 fUV;
layout (location = 1) out vec3 fNormal;
layout (location = 2) out flat int fMaterialId;
layout (location = 3) out vec4 fTangent;

#include("vke:assets/global/shaders/vke_sets.gdef")
#include("vke:assets/global/shaders/instance.gdef")

void main() {
    MeshInstance mi = u_InstanceBuffer.meshInstances[gl_InstanceIndex];
    mat4 localMatrix = mi.local;
    mat3 modelMatrix = mat3(localMatrix);
    mat3 normalMatrix = transpose(inverse(modelMatrix));

    fUV = inUV;
    fNormal = normalize(normalMatrix * inNormal);
    fTangent = vec4(normalize(modelMatrix * inTangent.xyz), inTangent.w);
    fMaterialId = mi.matId;

    gl_Position = frameData.camera.projection * frameData.camera.view * localMatrix * vec4(inPos, 1.0f);
}