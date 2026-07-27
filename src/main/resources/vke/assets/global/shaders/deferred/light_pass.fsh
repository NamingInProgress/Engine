#version 450

layout (location = 0) in vec2 fUV;

layout (location = 0) out vec4 FragColor;

layout (set = 2, binding = 0) uniform sampler2D u_NormalTex;
layout (set = 2, binding = 1) uniform sampler2D u_AlbedoSpecTex;
layout (set = 2, binding = 2) uniform sampler2D u_DepthTex;

#include("vke:assets/global/shaders/vke_sets.gdef")

struct Light {
    vec4 pos;
    vec4 color;
};

#Static
layout (std430, set = 3, binding = 0) readonly buffer Lights {
    #DefaultSize(10)
    Light lights[];
} u_Lights;

vec3 reconstructWorldPosition() {
    float depth = vktexture(u_DepthTex, fUV).r;

    vec4 clipPos = vec4(fUV * 2.0 - 1.0, depth, 1.0);

    vec4 worldPos = frameData.camera.inverse_view * frameData.camera.inverse_projection * clipPos;

    return worldPos.xyz / worldPos.w;
}

void main() {
    vec3 albedo = vktexture(u_AlbedoSpecTex, fUV).rgb;

    vec3 normal = normalize(vktexture(u_NormalTex, fUV).xyz * 2.0 - 1.0);

    vec3 fragPos = reconstructWorldPosition();

    Light light = u_Lights.lights[0];

    vec3 lightDir = normalize(vec3(light.pos) - fragPos);

    float NdotL = max(dot(normal, lightDir), 0.0);

    vec3 lighting = albedo * 0.1;
    lighting += albedo * vec3(light.color) * NdotL;

    FragColor = vec4(lighting, 1.0);
}