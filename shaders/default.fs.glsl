#version 330 core

in vec2 fTex;
in vec3 fNor;
in vec4 fCol;
in vec3 fPos;

struct MaterialMap {
    int useSampler;
    sampler2D sampler;
};

uniform MaterialMap baseColor;
uniform MaterialMap normalMap;
uniform MaterialMap aoMap;
uniform MaterialMap metalnessMap;
uniform MaterialMap roughnessMap;

uniform int flipTexCordX;
uniform int flipTexCordY;

uniform int haveTangents;

uniform float shineness;
out vec4 finalColor;

in mat3 TBN;
in vec3 LD;

void main()
{
    vec3 N = fNor;

    vec2 texCod = fTex;
    if(flipTexCordX > 0) { texCod.x = 1 - texCod.x; }
    if(flipTexCordY > 0) { texCod.y = 1 - texCod.y; }

    if(haveTangents > 0 && normalMap.useSampler > 0) {
        N = TBN * normalize((2 * texture(normalMap.sampler, texCod).rgb - 1));
    }

    N = normalize(N);
    vec3 L = normalize(LD);
    vec3 V = normalize(-fPos);
    vec3 H = normalize(L + V);

    float lightFactor = max(dot(N, L), 0);
    float specularFactor = 0;
    if(lightFactor > 0)
        specularFactor = pow(max(dot(reflect(-L, N), H), 0.0), shineness);

    finalColor = vec4(0.15f, 0.15f, 0.15f, 1.0f) * lightFactor + specularFactor;

    if(baseColor.useSampler > 0) {
        finalColor = texture(baseColor.sampler, texCod) * lightFactor;
    }

    if(aoMap.useSampler > 0) {
        finalColor *= texture(aoMap.sampler, texCod).r;
    }

    vec4 finalSpecularColor = vec4(1) * specularFactor;

    if(roughnessMap.useSampler > 0) {
        finalSpecularColor *= (1-texture(roughnessMap.sampler, texCod).r);
    }

    if(metalnessMap.useSampler > 0) {
        finalColor += (finalSpecularColor) * (1-texture(metalnessMap.sampler, texCod).r);
    }


//    vec4 finalBaseColor = texture(baseColor, texCod) * texture(aoMap, texCod).r;
//    finalColor = finalBaseColor * lightFactor + specularFactor * (1-texture(roughnessMap, texCod).r) * texture(metalnessMap, texCod).r + finalBaseColor * 0.2;
}