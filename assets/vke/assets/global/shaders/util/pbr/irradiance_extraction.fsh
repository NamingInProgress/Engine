#version 450

#include("vke:assets/global/shaders/vke_sets.gdef")
#include("vke:assets/global/shaders/util/pbr/cubemap_util.gdef")

layout(set = 2, binding = 0) uniform samplerCube u_Skybox;

layout(push_constant) uniform FaceData {
    int face;
} pc;

layout(location = 0) in vec2 uv;
layout(location = 0) out vec4 outColor;

void main() {
    vec3 normal = getDirection(uv, pc.face);

    vec3 irradiance = vec3(0.0);

    vec3 up = vec3(0, 1, 0);
    vec3 right = normalize(cross(normal, up));
    up = normalize(cross(normal, right));

    float sampleDelta = 0.025;
    float nr_samples = 0.0;

    for (float phi = 0; phi < 2 * PI; phi += sampleDelta) {
        for (float theta = 0; theta < 0.5 * PI; theta += sampleDelta) {
            vec3 tangentSample = vec3(sin(theta) * cos(phi),  sin(theta) * sin(phi), cos(theta));

            vec3 sampleVec = tangentSample.x * right + tangentSample.y * up + tangentSample.z * normal;

            irradiance += handleInvalidValues(texture(u_Skybox, sampleVec).rgb * cos(theta) * sin(theta));
            nr_samples++;
        }
    }

    irradiance = PI * irradiance * (1.0 / float(nr_samples));

    outColor = vec4(irradiance, 1);
}