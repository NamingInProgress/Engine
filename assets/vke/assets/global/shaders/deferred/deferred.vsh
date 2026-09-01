#version 450

layout (location = 0) in vec3 inPos;
layout (location = 1) in vec3 inNormal;
layout (location = 2) in vec2 inUV;
layout (location = 3) in int materialId;
layout (location = 4) in vec4 inTangent;

layout (location = 0) out vec2 fUV;
layout (location = 1) out vec3 fNormal;
layout (location = 2) out flat int fMaterialId;
layout (location = 3) out vec4 fTangent;

#include("vke:assets/global/shaders/vke_sets.gdef")

layout(push_constant) uniform constants {
    mat4 local;
} PushConstants;

layout (std430, set = 3, binding = 0) readonly buffer Transforms {
    #DefaultSize(1048576)
    mat4 local[];
} transforms;

void main() {
    mat4 localMatrix = transforms.local[gl_InstanceIndex];
    mat3 modelMatrix = mat3(localMatrix);
    mat3 normalMatrix = transpose(inverse(modelMatrix));

    fUV = inUV;
    fNormal = normalize(normalMatrix * inNormal);
    fTangent = vec4(normalize(modelMatrix * inTangent.xyz), inTangent.w);
    fMaterialId = materialId;

    gl_Position = frameData.camera.projection * frameData.camera.view * transforms.local[gl_InstanceIndex] * vec4(inPos, 1.0f);
}