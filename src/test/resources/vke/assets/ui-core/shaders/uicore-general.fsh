#version 450

#extension GL_EXT_nonuniform_qualifier : require

layout(location=0) in vec3 fpos;
layout(location=1) in vec4 fcolor;
layout(location=2) in vec2 fuv;
layout(location=3) in flat int ftex;
layout(location=4) in flat vec4 fclip;

layout(location=0) out vec4 outColor;

//layout(set=1,binding=0) uniform sampler2D textures[];

bool outsideRect(vec4 rect, vec2 pt) {
    return pt.x < rect.x && pt.y < rect.y && pt.x > rect.x + rect.z && pt.y > rect.y + rect.w;
}

void main() {
    if (outsideRect(fclip, fpos.xy)) {
        discard;
    }

    vec4 col;

    if (ftex >= 0) {
        //col = mix(texture(textures[nonuniformEXT(ftex)], vec2(fuv.x, 1.0 - fuv.y)), fcolor, 0.5);
    } else {
        col = fcolor;
    }
    outColor = col;
}