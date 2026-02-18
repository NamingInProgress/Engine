#version 450

layout(location = 0) in vec4 col;
layout(location = 1) flat in int vVertexID;

layout (location = 0) out vec4 color;

layout (set = 0, binding = 0) uniform Globals {
    vec4 fColor;
    vec4 sColor;
    float time;
} globals;

void main() {
    float perc = abs(sin(0.001 * globals.time * 3.2));

    if (vVertexID < 3) {
        color = mix(col, globals.sColor, perc);
    } else {
        color = mix(col, globals.fColor, perc);
    }

    //color = vec4(vec3(perc), 1);
}