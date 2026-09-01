#version 450

layout (location = 0) in vec2 inBezierPoints;

#include("vke:assets/global/shaders/vke_sets.gdef")

void main() {
    float y = inBezierPoints.y;
    float x = inBezierPoints.x;
    bool fill = y > x * x;

    if (!fill) discard;
}