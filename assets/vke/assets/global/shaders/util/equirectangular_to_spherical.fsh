#version 450

#include("vke:assets/global/shaders/vke_sets.gdef")

layout(set = 2, binding = 0) uniform sampler2D equirectangularMap;

layout(push_constant) uniform FaceData {
    int face;
} pc;

layout(location = 0) in vec2 uv;
layout(location = 0) out vec4 outColor;

const float PI = 3.14159265359;

vec3 getDirection(vec2 uv, int face) {
    vec2 p = uv * 2.0 - 1.0;
    p.y = -p.y;

    switch (face) {
        case 0: return normalize(vec3( 1.0, -p.y, -p.x)); // +X
        case 1: return normalize(vec3(-1.0, -p.y,  p.x)); // -X

        case 2: return normalize(vec3( p.x,  1.0,  p.y)); // +Y
        case 3: return normalize(vec3( p.x, -1.0, -p.y)); // -Y

        case 4: return normalize(vec3( p.x, -p.y,  1.0)); // +Z
        case 5: return normalize(vec3(-p.x, -p.y, -1.0)); // -Z
    }

    return vec3(0.0);
}

void main() {
    vec3 dir = getDirection(uv, pc.face);

    vec2 sphericalUV = vec2(
            atan(dir.z, dir.x) / (2.0 * PI) + 0.5,
            asin(dir.y) / PI + 0.5
    );

    outColor = vec4(
            texture(equirectangularMap, sphericalUV).rgb,
            1.0
    );
}