#version 450

layout(location = 0) in vec2 inLocalPos;
layout(location = 1) in vec2 inUV;

layout(location = 0) out vec4 outColor;
layout(location = 1) out vec2 outUV;
layout(location = 2) out flat int outTexId;

layout(location = 3) out flat vec2 outCenter;
layout(location = 4) out flat vec2 outHalfSize;
layout(location = 5) out flat vec2 outRadii;
layout(location = 6) out vec2 outFragPos;
layout(location = 7) out flat float outStrokeWidth;

struct RectInstance {
    vec2 center;
    vec2 halfSize;

    vec4 color;
    vec2 uv[4]; //gl_VertexIndex

    vec2 radii;

    float strokeWidth;

    int texId;

    uint transformIndex;
    uint clipIndex;
};

layout(set = 0, binding = 0, std430) readonly buffer InstanceBuffer {
    RectInstance instances[];
} InstanceData;

layout(set = 1, binding = 0, std430) readonly buffer TransformBuffer {
    mat4 matrices[];
} Transform;

layout(set = 2, binding = 0, std430) readonly buffer ClipBuffer {
    vec4 rects[];
} Clip;

layout(push_constant) uniform constants {
    mat4 projection;
} PushConstants;

void main() {
    RectInstance instance = InstanceData.instances[gl_InstanceIndex];
    mat4 transform = Transform.matrices[instance.transformIndex];
    vec4 clip = Clip.rects[instance.clipIndex];
    vec2 uv = instance.uv[gl_VertexIndex]; //this shouldnt overflow...its not worth doing the extra min() call here

}