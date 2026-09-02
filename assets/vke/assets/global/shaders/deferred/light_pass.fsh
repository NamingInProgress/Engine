#version 450

layout (location = 0) in vec2 fUV;

layout (location = 0) out vec4 FragColor;

layout (set = 3, binding = 0) uniform sampler2D u_NormalTex;
layout (set = 3, binding = 1) uniform sampler2D u_MaterialIdxTex;
layout (set = 3, binding = 2) uniform sampler2D u_MeshUvsTex;
layout (set = 3, binding = 3) uniform sampler2D u_DepthTex;

#include("vke:assets/global/shaders/vke_sets.gdef")
#include("vke:assets/global/shaders/materials.gdef")
#include("vke:assets/global/shaders/lights.gdef")

vec3 reconstructWorldPosition() {
    float depth = vktexture(u_DepthTex, fUV).r;
    vec4 clip = vec4(fUV * 2.0 - 1.0, depth, 1.0);
    vec4 view = frameData.camera.inverse_projection * clip;
    view /= view.w;
    vec4 world = frameData.camera.inverse_view * view;
    return world.xyz;
}

void main() {
    vec3 normal = vktexture(u_NormalTex, fUV).xyz;
    int materialIdx = int(texelFetch(u_MaterialIdxTex, ivec2(gl_FragCoord.xy), 0).r);
    vec2 meshUV = vktexture(u_MeshUvsTex, fUV).xy;
    vec3 fragPos = reconstructWorldPosition();

    if (materialIdx == NO_MATERIAL) {
        discard;
    }

    MaterialInputs mat = evaluateMaterial(materialIdx, meshUV, vec3(0, 0, 1), vec4(0, 0, 1, 1));
    mat.normal = normal;

    FragColor = vec4(calculateLighting(normal, normalize(frameData.camera.position.xyz - fragPos), fragPos, mat), 1);
//    FragColor = vec4(1);
}