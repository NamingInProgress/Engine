#version 450

layout (location = 0) in vec3 inPos;
layout (location = 1) in vec4 inColor;
layout (location = 2) in vec2 inUV;
layout (location = 3) in int texId;
//roundrect specific stuff
layout (location = 4) in vec2 inCenter;
layout (location = 5) in vec2 inHalfSize;
layout (location = 6) in vec2 inRadii;
layout (location = 7) in float inStrokeWidth;


layout (location = 0) out vec4 outColor;
layout (location = 1) out vec2 UV;
layout (location = 2) out flat int outTexId;

layout (location = 3) out flat vec2 outCenter;
layout (location = 4) out flat vec2 outHalfSize;
layout (location = 5) out flat vec2 outRadii;
layout (location = 6) out vec2 outFragPos;
layout (location = 7) out flat float outStrokeWidth;

layout (push_constant) uniform constants {
    mat4 world;
    mat4 translation;
} PushConstants;

void main() {
    vec4 worldPos = PushConstants.translation * vec4(inPos, 1.0);
    gl_Position = PushConstants.world * worldPos;

    outColor = inColor;
    UV = inUV;
    outTexId = texId;
    outCenter = inCenter;
    outHalfSize = inHalfSize;
    outRadii = inRadii;
    outStrokeWidth = inStrokeWidth;

    outFragPos = worldPos.xy;
}