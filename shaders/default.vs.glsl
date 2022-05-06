#version 330 core
layout (location=0) in vec3 aPos;
layout (location=1) in vec2 aTex;
layout (location=2) in vec3 aNor;
layout (location=3) in vec4 aCol;

uniform mat4 mesh;
uniform mat4 projection;
uniform mat4 transform;
uniform mat4 view;

out vec2 fTex;
out vec3 fNor;
out vec4 fCol;
out vec3 fPos;

void main()
{
    fTex = aTex;
    mat4 finalTransform = transform * mesh;
    fNor = mat3(transpose(inverse(finalTransform))) * aNor;
    fCol = aCol;
    vec4 world = finalTransform * vec4(aPos, 1.0f);
    fPos = world.xyz;
    gl_Position =  projection * view * world;
}