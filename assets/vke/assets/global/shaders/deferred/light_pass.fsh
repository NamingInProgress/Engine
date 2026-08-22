#version 450

layout (location = 0) in vec2 fUV;
layout (location = 0) out vec4 FragColor;

layout (set = 2, binding = 0) uniform sampler2D u_NormalTex;
layout (set = 2, binding = 1) uniform sampler2D u_AlbedoSpecTex;
layout (set = 2, binding = 2) uniform sampler2D u_DepthTex;

#include("vke:assets/global/shaders/vke_sets.gdef")

struct Light {
    vec3 pos;
    float range;
    vec3 color;
    float intensity;
};

#Static
layout (std430, set = 3, binding = 0) readonly buffer Lights {
    int lightCount;
#DefaultSize(1000)
    Light lights[];
} u_Lights;

vec3 reconstructWorldPosition() {
    float depth = vktexture(u_DepthTex, fUV).r;
    vec4 clip = vec4(fUV * 2.0 - 1.0, depth, 1.0);
    vec4 view = frameData.camera.inverse_projection * clip;
    view /= view.w;
    vec4 world = frameData.camera.inverse_view * view;
    return world.xyz;
}

void main() {
    vec3 albedo = vktexture(u_AlbedoSpecTex, fUV).rgb;
    vec3 normal = normalize(vktexture(u_NormalTex, fUV).xyz);
    vec3 fragPos = reconstructWorldPosition();

    vec3 lighting = albedo * 0.1; // Ambient

    for (int i = 0; i < u_Lights.lightCount; ++i)
    {
        Light light = u_Lights.lights[i];

        vec3 lightVec = light.pos - fragPos;
        float distSq = dot(lightVec, lightVec);
        float rangeSq = light.range * light.range;

        if (distSq >= rangeSq)
        continue;

        float distance = sqrt(distSq);
        vec3 lightDir = lightVec / distance;

        float NdotL = max(dot(normal, lightDir), 0.0);
        if (NdotL <= 0.0)
        continue;

        float attenuation = 0.0;

        // ====================================================
        // OPTION A: STYLIZED / SMOOTH FALLOFF (Non-Physical)
        // Use this if you want a simple, game-y light that
        // fades very evenly and is easy to control.
        // ====================================================

        // Linear fade from 1.0 at center to 0.0 at range edge
        float normalizedDist = distance / light.range;
        attenuation = clamp(1.0 - normalizedDist, 0.0, 1.0);
        // Optional: square it for a slightly nicer curve
        attenuation *= attenuation;


        // ====================================================
        // OPTION B: UNREAL ENGINE 4 PBR (Physical)
        // (Comment out Option A and uncomment this to use it)
        // ====================================================
        /*
        // If your hotspot is still too bright, increase minRadius!
        // Try setting it to 10% of your range (light.range * 0.1)
        float minRadius = max(light.range * 0.05, 0.5);
        float invSqrAtten = 1.0 / max(distSq, minRadius * minRadius);

        float distOverRangeSq = distSq / rangeSq;
        float window = clamp(1.0 - (distOverRangeSq * distOverRangeSq), 0.0, 1.0);
        attenuation = invSqrAtten * (window * window);
        */
        // ====================================================

        vec3 radiance = light.color * light.intensity * attenuation;
        lighting += albedo * radiance * NdotL;
    }

    // ====================================================
    // POST PROCESSING
    // This stops the hotspot from clipping into a harsh shape
    // and brightens up the dark falloff areas.
    // ====================================================

    // 1. Reinhard Tonemapping (Compress HDR to 0.0-1.0)
    lighting = lighting / (lighting + vec3(1.0));

    // 2. Gamma Correction (Linear space to sRGB monitor space)
    lighting = pow(lighting, vec3(1.0 / 2.2));

    FragColor = vec4(lighting, 1.0);
}