#version 330 core

out vec4 color;

uniform samplerCube env;
in vec3 texCords;

void main()
{
    color = texture(env, texCords);
}