#version 450

#include("vke:assets/global/shaders/vke_sets.gdef")

layout(location = 0) in vec2 v_TexCoord;

layout(location = 0) out vec4 outColor;

layout (set = 2, binding = 0) uniform sampler2D u_InTex;

vec3 UpsampleTent(vec2 uv, vec2 texelSize, float sampleRadius) {
    vec4 d = texelSize.xyxy * vec4(-1.0, -1.0, 1.0, 1.0) * sampleRadius;

    vec3 result = vktexture(u_InTex, uv + d.xy).rgb * 1.0;
    result += vktexture(u_InTex, uv + vec2(0.0, d.y)).rgb * 2.0;
    result += vktexture(u_InTex, uv + vec2(d.z, d.y)).rgb * 1.0;

    result += vktexture(u_InTex, uv + vec2(d.x, 0.0)).rgb * 2.0;
    result += vktexture(u_InTex, uv).rgb * 4.0;
    result += vktexture(u_InTex, uv + vec2(d.z, 0.0)).rgb * 2.0;

    result += vktexture(u_InTex, uv + vec2(d.x, d.w)).rgb * 1.0;
    result += vktexture(u_InTex, uv + vec2(0.0, d.w)).rgb * 2.0;
    result += vktexture(u_InTex, uv + d.zw).rgb * 1.0;

    return result * (1.0 / 16.0);
}

void main() {
    vec2 texelSize = 1.0 / vec2(textureSize(u_InTex, 0));
    outColor = vec4(UpsampleTent(v_TexCoord, texelSize, 1.0), 1.0);
}