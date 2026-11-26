#version 450

#include("vke:assets/global/shaders/vke_sets.gdef")
#include("vke:assets/global/shaders/util/pbr/cubemap_util.gdef")

layout(set = 2, binding = 0) uniform sampler2D equirectangularMap;

layout(push_constant) uniform FaceData {
    int face;
} pc;

layout(location = 0) in vec2 uv;
layout(location = 0) out vec4 outColor;

void main() {
    vec3 dir = getDirection(uv, pc.face);

    vec2 sphericalUV = vec2(
            atan(dir.z, dir.x) / (2.0 * PI) + 0.5,
            asin(dir.y) / PI + 0.5
    );

    outColor = handleInvalidValues(vec4(
            texture(equirectangularMap, sphericalUV).rgb,
            1.0
    ));
}