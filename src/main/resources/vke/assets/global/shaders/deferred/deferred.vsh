#version 450

layout (location = 0) in vec3 inPos;
layout (location = 1) in vec3 inNormal;
layout (location = 2) in vec2 inUV;
layout (location = 3) in int materialId;

layout (location = 0) out vec2 fUV;
layout (location = 1) out vec3 fNormal;
layout (location = 2) out flat int fMaterialId;

#include("vke:assets/global/shaders/vke_sets.gdef")

layout(push_constant) uniform constants {
    mat4 local;
} PushConstants;

layout (std430, set = 3, binding = 0) readonly buffer Transforms {
    #DefaultSize(1048576)
    mat4 local[];
} transforms;

void main() {
    vec4 pos = frameData.camera.projection * frameData.camera.view * transforms.local[gl_InstanceIndex] * vec4(inPos, 1.0f);
    gl_Position = pos;

    fUV = inUV;
    mat3 normalMatrix = transpose(inverse(mat3(transforms.local[gl_InstanceIndex])));
    fNormal = normalize(normalMatrix * inNormal);
    fMaterialId = materialId;
}