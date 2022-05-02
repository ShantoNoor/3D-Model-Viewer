#version 330 core

in vec3 fPos;
out vec4 color;

void Line(vec2 p1, vec2 p2) {
    vec2 p3 = fPos.xy;
    vec2 p12 = p2 - p1;
    vec2 p13 = p3 - p1;

    float d = dot(p12, p13) / length(p12);
    vec2 p4 = p1 + normalize(p12) * d;

    if (length(p4 - p3) < 0.001f) {
        color += vec4(0.0, 1.0, 0.0, 1.0);
    }
}

void main()
{
    color = vec4(0);
    Line(vec2(0), vec2(0, 1));
    Line(vec2(0), vec2(1, 0));
}