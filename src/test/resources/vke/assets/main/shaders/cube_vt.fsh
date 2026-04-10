#version 450

layout(location = 0) flat in vec4 col;
layout(location = 1) in vec3 normal;

layout (location = 0) out vec4 color;

void main() {
    // Hardcoded light direction (normalize just to be safe)
    vec3 lightDir = normalize(vec3(0.5, 1.0, 0.3));

    // Normalize incoming normal
    vec3 n = normalize(normal);

    // Lambert diffuse lighting
    float diff = max(dot(n, lightDir), 0.0);

    // Add a bit of ambient so it's not completely dark
    float ambient = 0.2;

    float lighting = ambient + diff;

    color = vec4(col.rgb * lighting, col.a);
}