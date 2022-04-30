#type vertex
#version 330 core

layout (location=0) in vec3 aPos;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 transform;

out vec4 fPos;

void main()
{
    mat4 world = projection * view * transform;
    fPos = vec4(aPos, 1.0f);
    gl_Position = fPos;
}

#type fragment
#version 330 core

in vec4 fPos;
out vec4 color;

void Line(vec2 p1, vec2 p2) {
    vec2 p3 = vec2(0,0);
    vec2 p12 = p2 - p1;
    vec2 p13 = p3 - p1;

    float d = dot(p12, p13) / length(p12);
    vec2 p4 = p1 + normalize(p12) * d;

    if (length(p4 - p3) < 0.01f) {
        color += vec4(0.0, 1.0, 0.0, 1.0);
    }
}

void main()
{
    Line(fPos.xy, fPos.yz);
}