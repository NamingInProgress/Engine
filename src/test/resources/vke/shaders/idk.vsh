#version 450
#extension GL_EXT_buffer_reference : require

layout (location = 0) out vec4 outColor;
layout (location = 1) out vec2 outUV;

struct Vertex {
    vec3 position;
    float pad0;
    vec4 color;
    vec2 uv;
};

layout(buffer_reference, std430) readonly buffer VertexBuffer {
    Vertex vertices[];
};

layout(push_constant) uniform constants {
    VertexBuffer vertexBuffer;
    mat4 world;
} PushConstants;

void main() {
    Vertex v = PushConstants.vertexBuffer.vertices[gl_VertexIndex];

    gl_Position = PushConstants.world * vec4(v.position, 1.0f);
    outColor = v.color;
    outUV = v.uv;
}