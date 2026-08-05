#version 450

layout (location = 0) in vec4 inColor;
layout (location = 1) in vec2 inBezierPoints;

layout (location = 0) out vec4 outColor;

#include("vke:assets/global/shaders/vke_sets.gdef")

void main() {
    float y = inBezierPoints.y;
    float x = inBezierPoints.x;
    bool fill = y > x * x;

    if (gl_FrontFacing) fill = !fill;
    if (!fill) discard;

    outColor = vec4(1);
}