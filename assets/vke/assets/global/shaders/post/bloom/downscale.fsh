#version 450

layout(location = 0) in vec2 uv;

layout (location = 0) out vec4 outColor;

#include("vke:assets/global/shaders/vke_sets.gdef")

layout (set = 2, binding = 0) uniform sampler2D u_InTex;

vec3 Downsample13Tap(vec2 texelSize) {
    // 4 inner corners, 4 outer edges, 4 outer corners, 1 center
    vec3 A = vktexture(u_InTex, uv + texelSize * vec2(-2, -2)).rgb;
    vec3 B = vktexture(u_InTex, uv + texelSize * vec2( 0, -2)).rgb;
    vec3 C = vktexture(u_InTex, uv + texelSize * vec2( 2, -2)).rgb;
    vec3 D = vktexture(u_InTex, uv + texelSize * vec2(-2,  0)).rgb;
    vec3 E = vktexture(u_InTex, uv).rgb; // Center
    vec3 F = vktexture(u_InTex, uv + texelSize * vec2( 2,  0)).rgb;
    vec3 G = vktexture(u_InTex, uv + texelSize * vec2(-2,  2)).rgb;
    vec3 H = vktexture(u_InTex, uv + texelSize * vec2( 0,  2)).rgb;
    vec3 I = vktexture(u_InTex, uv + texelSize * vec2( 2,  2)).rgb;
    vec3 J = vktexture(u_InTex, uv + texelSize * vec2(-1, -1)).rgb;
    vec3 K = vktexture(u_InTex, uv + texelSize * vec2( 1, -1)).rgb;
    vec3 L = vktexture(u_InTex, uv + texelSize * vec2(-1,  1)).rgb;
    vec3 M = vktexture(u_InTex, uv + texelSize * vec2( 1,  1)).rgb;

    // Weighted average (center box gets 0.5 weight, 4 corner boxes get 0.125 weight each)
    vec3 result = (D+E+I+H)*0.0625 + (A+B+G+F)*0.0625 + (B+C+F+H)*0.0625 + (E+F+I+H)*0.0625;
    result += (J+K+L+M)*0.125;
    return result;
}

void main() {
    vec2 texelSize = 1.0 / vec2(textureSize(u_InTex, 0));
    outColor = vec4(Downsample13Tap(texelSize), 1);
}