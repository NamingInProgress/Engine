#version 450

layout(location=0) in vec3 vpos;
layout(location=1) in vec4 vcol;
layout(location=2) in vec2 vuv;
layout(location=3) in int vtex;
layout(location=4) in int vtransform;
layout(location=5) in int vclip;

layout(location=0) out vec3 fpos;
layout(location=1) out vec4 fcolor;
layout(location=2) out vec2 fuv;
layout(location=3) out flat int ftex;
layout(location=4) out flat vec4 fclip;

layout(set = 1, binding = 0, std430) readonly buffer TransformBuffer {
    mat4 matrices[2048];
} Transform;

layout(set = 2, binding = 0, std430) readonly buffer ClipBuffer {
    vec4 rects[2048*4];
} Clip;

layout(push_constant) uniform constants {
    mat4 projection;
} PushConstants;

void main() {
    mat4 proj = PushConstants.projection;
    mat4 transform = Transform.matrices[vtransform];
    vec4 clipReect = Clip.rects[vclip];

    vec4 v4pos = vec4(vpos, 1.0);
    gl_Position = proj * transform * v4pos;

    vec4 localPos = transform * v4pos;
    fpos = localPos.xyz;
    fcolor = vcol;
    fuv = vuv;
    ftex = vtex;
    fclip = clipReect;
}