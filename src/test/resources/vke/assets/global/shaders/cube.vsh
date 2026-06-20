#version 450
#extension GL_EXT_buffer_reference : require

layout (location = 0) out vec4 outColor;

layout (set = 0, binding = 0) uniform Camera {
    mat4 proj;
    mat4 view;
} camera;

layout (set = 1, binding = 0) uniform sampler2D textures[];


struct Vertex {
    vec3 position;
    float pad0;
    vec4 color;
};

layout(buffer_reference, std430) readonly buffer VertexBuffer {
    Vertex vertices[];
};

layout(push_constant) uniform constants {
    VertexBuffer vertexBuffer;
    mat4 world;
    mat4 translation;
} PushConstants;

void main() {
    Vertex v = PushConstants.vertexBuffer.vertices[gl_VertexIndex];

    gl_Position = PushConstants.world * PushConstants.translation * vec4(v.position, 1.0f);
    outColor = v.color;
}