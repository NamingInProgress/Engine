#version 450

#include("vke:assets/global/shaders/vke_sets.gdef")
#include("vke:assets/global/shaders/util/pbr/cubemap_util.gdef")

layout(set = 2, binding = 0) uniform samplerCube u_Skybox;

layout(push_constant) uniform FaceData {
    int face;
    float roughness;
} pc;

layout(location = 0) in vec2 uv;
layout(location = 0) out vec4 outColor;

void main() {
    vec3 N = getDirection(uv, pc.face);
    vec3 R = N;
    vec3 V = R;

    const uint SAMPLE_COUNT = 1024u;
    float totalWeight = 0.0;
    vec3 prefilteredColor = vec3(0);

    for (uint i = 0u; i < SAMPLE_COUNT; i++) {
        vec2 Xi = Hammersley(i, SAMPLE_COUNT);
        vec3 H = ImportanceSampleGGX(Xi, N, pc.roughness);
        vec3 L = normalize(2.0 * dot(V, H) * H - V);

        float NdotL = max(dot(N, L), 0.0);
        if(NdotL > 0.0) {
            prefilteredColor += handleInvalidValues(texture(u_Skybox, L).rgb) * NdotL;
            totalWeight      += NdotL;
        }
    }
    prefilteredColor = prefilteredColor / totalWeight;

    outColor = vec4(prefilteredColor, 1.0);
}