#version 450

#extension GL_EXT_nonuniform_qualifier : enable

layout (location = 0) in vec4 inColor;
layout (location = 1) in vec2 UV;
layout (location = 2) flat in int texId;

layout (location = 3) flat in vec2 center;
layout (location = 4) flat in vec2 halfSize;
layout (location = 5) flat in vec2 radii;
layout (location = 6) in vec2 fragPos;
layout (location = 7) in flat float inStrokeWidth;

layout (location = 0) out vec4 outColor;

layout (set = 0, binding = 0) uniform sampler2D[4] textures;

float sdEllipticalRoundRect(vec2 p, vec2 b, vec2 r) {
    if (r.x <= 0.0 || r.y <= 0.0) {
        vec2 d = abs(p) - b;

        return length(max(d, 0.0)) + min(max(d.x, d.y), 0.0);
    }

    vec2 q = abs(p) - b + r;

    return min(max(q.x, q.y), 0.0) + length(max(q / r, 0.0)) * min(r.x, r.y) - min(r.x, r.y);
}

void main() {
    vec4 col;

    if (texId >= 0) {
        col = mix(
            texture(textures[nonuniformEXT(texId)], vec2(UV.x, 1.0 - UV.y)),
            inColor,
            0.5
        );
    } else {
        col = inColor;
    }

    vec2 p = fragPos - center;

    vec2 r = radii;

    vec2 b = halfSize;

    float sw = inStrokeWidth;
    float dist = sdEllipticalRoundRect(p, b, r);
    b -= vec2(sw, sw);
    float fix = 6;
    r = vec2(min(halfSize.x - sw, r.x - fix), min(halfSize.y - sw, r.y - fix));
    float tinyDist = sdEllipticalRoundRect(p, b, r);

    float aa = fwidth(dist);
    // Fill mask
    float fillAlpha = 1.0 - smoothstep(0.0, aa, dist);

    // Stroke mask
    float strokeAlpha = smoothstep(0.0, aa, tinyDist) * fillAlpha;


    // Branchless select:
    // sw == 0 -> fill
    // sw > 0  -> stroke
    float useStroke = step(0.0001, sw);

    float alpha = mix(fillAlpha, strokeAlpha, useStroke);

    outColor = vec4(col.rgb, col.a * alpha);
}