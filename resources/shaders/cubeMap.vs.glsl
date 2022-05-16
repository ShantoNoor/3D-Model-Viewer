#version 330 core

layout (location=0) in vec3 aPos;

uniform mat4 projection;
uniform mat4 view;

out vec3 texCords;

void main()
{
    mat4 modView = mat4(mat3(view));
    vec4 pos = projection * modView * vec4(aPos, 1.0);
    gl_Position = pos.xyww;
    texCords = vec3(aPos.x, aPos.y, aPos.z);
}