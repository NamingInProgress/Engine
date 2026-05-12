#version 460

#extension GL_EXT_nonuniform_qualifier : require

struct Light {
    vec3 pos;
};

layout (set = 0, binding = 0) uniform Camera {
    mat4 proj;
    mat4 view;
    Light l;
} camera;

layout (set = 1, binding = 0) uniform sampler2D textures[];

void main() {

}