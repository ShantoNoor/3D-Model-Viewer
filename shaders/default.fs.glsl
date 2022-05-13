#version 330 core

in vec2 fTex;
in vec3 fNor;
in vec4 fCol;
in vec3 fPos;

uniform sampler2D baseColor;
uniform sampler2D normalMap;
uniform sampler2D aoMap;
uniform sampler2D metalnessMap;
uniform sampler2D roughnessMap;

uniform int flipTexCordX;
uniform int flipTexCordY;

uniform int haveTangents;


uniform float shine;
out vec4 finalColor;

in mat3 TBN;
in vec3 LD;

void main()
{
    vec3 N = fNor;

    vec2 texCod = fTex;
    if(flipTexCordX > 0) { texCod.x = 1 - texCod.x; }
    if(flipTexCordY > 0) { texCod.y = 1 - texCod.y; }

//    if(haveTangents > 0) {
//        N = TBN * normalize((2 * texture(normalMap, texCod).rgb - 1));
//    }

    N = normalize(N);
    vec3 L = normalize(LD);
    vec3 V = normalize(-fPos);
    vec3 H = normalize(L + V);

    float lightFactor = max(dot(N, L), 0);
    float specularFactor = 0;
    if(lightFactor > 0)
        specularFactor = pow(max(dot(reflect(-L, N), H), 0.0), shine);

    finalColor = vec4(0.15f, 0.15f, 0.15f, 1.0f) * lightFactor + specularFactor;

//    vec4 finalBaseColor = texture(baseColor, texCod) * texture(aoMap, texCod).r;
//    finalColor = finalBaseColor * lightFactor + specularFactor * (1-texture(roughnessMap, texCod).r) * texture(metalnessMap, texCod).r + finalBaseColor * 0.2;
}