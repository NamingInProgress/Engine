#version 450
#extension GL_EXT_buffer_reference : require

layout (location = 0) out vec4 outColor;

layout (set = 0, binding = 0) uniform Globals {
    vec3 camera;
    mat4 viewProj;
    float time;
} globals;

layout (set = 0, binding = 1) uniform sampler2D tex0;

void main() {

}