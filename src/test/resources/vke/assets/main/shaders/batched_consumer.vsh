#version 450

layout (location = 0) in vec3 inPos;
layout (location = 1) in vec4 inColor;
layout (location = 2) in vec2 inUV;
layout (location = 3) in int texId;

layout (location = 0) out vec4 outColor;
layout (location = 1) out vec2 UV;
layout (location = 2) out flat int outTexId;

layout (push_constant) uniform constants {
    mat4 world;
    mat4 translation;
} PushConstants;

void main() {
    gl_Position = PushConstants.world * PushConstants.translation * vec4(inPos, 1);
    outColor = inColor;
    UV = inUV;
    outTexId = texId;
}