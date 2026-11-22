#version 450

#include("vke:assets/global/shaders/vke_sets.gdef")

layout(location = 0) out vec3 direction;

const vec3 vertices[36] = vec3[](
        // Back
        vec3(-1, -1, -1), vec3( 1, -1, -1), vec3( 1,  1, -1),
        vec3( 1,  1, -1), vec3(-1,  1, -1), vec3(-1, -1, -1),

        // Front
        vec3(-1, -1,  1), vec3( 1,  1,  1), vec3( 1, -1,  1),
        vec3( 1,  1,  1), vec3(-1, -1,  1), vec3(-1,  1,  1),

        // Left
        vec3(-1, -1, -1), vec3(-1,  1,  1), vec3(-1, -1,  1),
        vec3(-1,  1,  1), vec3(-1, -1, -1), vec3(-1,  1, -1),

        // Right
        vec3( 1, -1, -1), vec3( 1, -1,  1), vec3( 1,  1,  1),
        vec3( 1,  1,  1), vec3( 1,  1, -1), vec3( 1, -1, -1),

        // Bottom
        vec3(-1, -1, -1), vec3( 1, -1,  1), vec3( 1, -1, -1),
        vec3( 1, -1,  1), vec3(-1, -1, -1), vec3(-1, -1,  1),

        // Top
        vec3(-1,  1, -1), vec3( 1,  1, -1), vec3( 1,  1,  1),
        vec3( 1,  1,  1), vec3(-1,  1,  1), vec3(-1,  1, -1)
);

void main() {
    vec3 position = vertices[gl_VertexIndex];

    direction = position;

    mat4 rotationView = mat4(mat3(frameData.camera.view));
    vec4 clip = frameData.camera.projection * rotationView * vec4(position, 1.0);

    gl_Position = clip.xyww;
}