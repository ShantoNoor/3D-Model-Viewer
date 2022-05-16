#version 330 core

in vec2 fTex;
in vec3 fNor;
in vec4 fCol;
in vec3 fPos;

struct MaterialMap {
    int useSampler;
    sampler2D sampler;
};

uniform vec4 ambientColor;
uniform vec4 diffuseColor;
uniform vec4 specularColor;
uniform vec4 emissiveColor;

uniform float roughnessFactor;
uniform float metallicFactor;
uniform float shininess;
uniform float emissiveIntensity;

uniform MaterialMap baseColorMap;
uniform MaterialMap normalMap;
uniform MaterialMap aoMap;
uniform MaterialMap metallicMap;
uniform MaterialMap roughnessMap;

uniform samplerCube env;

uniform int flipTexCordX;
uniform int flipTexCordY;

uniform int haveTangents;
uniform vec3 cameraPos;

out vec4 finalColor;

in mat3 TBN;

out vec3 texDir;

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
    vec3 L = normalize(cameraPos);
    vec3 V = normalize(cameraPos-fPos);
    vec3 H = normalize(L + V);

    float lightFactor = max(dot(N, L), 0);
    float specularFactor = 0;
    if(lightFactor > 0) {
        specularFactor = pow(max(dot(reflect(-L, N), H), 0.0), shininess);
    }

    vec4 diffColor = diffuseColor * lightFactor;
    vec4 specColor = specularColor * specularFactor;

    if(baseColorMap.useSampler > 0 && lightFactor > 0) {
        diffColor = texture(baseColorMap.sampler, texCod) * lightFactor;
        specColor = vec4(1) * specularFactor;
    }

    if(aoMap.useSampler > 0 && lightFactor > 0) {
        diffColor *= texture(aoMap.sampler, texCod).r;
    }

    float roughness = 1 - roughnessFactor;
    if(roughnessMap.useSampler > 0 && specularFactor > 0) {
        roughness = 1 - texture(roughnessMap.sampler, texCod).r;
    }
    specColor *= roughness;

    float metalic = metallicFactor;
    if(metallicMap.useSampler > 0 && specularFactor > 0) {
        metalic = texture(metallicMap.sampler, texCod).r;
    }
    specColor *= metalic;

    finalColor = diffColor
                    + specColor * ((mix(texture(env, reflect(-V, N)), diffColor, 0.6)) * 0.3)
                    + ((mix(texture(env, N), diffColor, 0.6)) * 0.3) + (emissiveColor * emissiveIntensity);
}