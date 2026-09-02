#version 450

layout(location = 0) in vec2 uv;

layout (location = 0) out vec4 outColor;

#include("vke:assets/global/shaders/vke_sets.gdef")

layout (set = 2, binding = 0) uniform sampler2D u_InTex;

void main() {
    vec3 color = vktexture(u_InTex, uv).rgb;

    float brightness = dot(color, vec3(0.2126, 0.7152, 0.0722));

    float threshold = 1.0;
    float knee = 0.5;

    float soft = brightness - threshold + knee;
    soft = clamp(soft, 0.0, 2.0 * knee);
    soft = soft * soft / (4.0 * knee + 0.00001);

    float contribution = max(soft, brightness - threshold);
    contribution /= max(brightness, 0.00001);

    vec3 bloom = color * contribution;

    outColor = vec4(bloom, 1.0);
}