#version 330 core
layout (location=0) in vec3 aPos;
layout (location=1) in vec2 aTex;
layout (location=2) in vec3 aNor;
layout (location=3) in vec4 aCol;
layout (location=4) in vec3 aTan;
layout (location=5) in vec3 aBtan;

uniform mat4 mesh;
uniform mat4 projection;
uniform mat4 transform;
uniform mat4 view;
uniform mat4 rotate;

uniform vec3 camPos;
uniform vec3 lightF;

out vec2 fTex;
out vec3 fNor;
out vec4 fCol;
out vec3 fPos;

out mat3 TBN;
out vec3 cp;
out vec3 light;

void main()
{
    fTex = aTex;
    fCol = aCol;

    mat4 model = rotate * transform * mesh;
    fNor = normalize(mat3(transpose(inverse(model))) * aNor);

    fPos = vec3(model * vec4(aPos, 1.0f));
    cp = camPos;
    light = lightF;

    gl_Position =  projection * view  * model * vec4(aPos, 1.0f);;

    vec3 T = normalize(mat3(transpose(inverse(model))) * aTan);
    vec3 B = normalize(mat3(transpose(inverse(model))) * aBtan);
    vec3 N = normalize(mat3(transpose(inverse(model))) * aNor);
    TBN = transpose(mat3(T, B, N));
}