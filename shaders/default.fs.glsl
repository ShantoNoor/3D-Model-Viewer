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

out vec4 finalColor;

in vec3 cp;
in mat3 TBN;
in vec3 light;

void main()
{
//    vec3 N = fNor;
    vec2 texCod = fTex;

    if(flipTexCordX > 0) { texCod.x = 1 - texCod.x; }
    if(flipTexCordY > 0) { texCod.y = 1 - texCod.y; }

    vec3 N = TBN * normalize( (2 * texture(normalMap, texCod).rgb - 1) );
    vec3 L = normalize(light);
    vec3 V = normalize(cp - fPos);

    float lightFactor = max(dot(N, L), 0);
    float specularFactor = max(dot(reflect(-L, N), V), 0.0);
    specularFactor = pow(specularFactor, 25);

    vec4 color = vec4(vec3(0.15f, 0.15f, 0.15f), 1.0f);
    if(fCol != vec4(0.0f)) color = fCol;

    vec4 finalBaseColor = texture(baseColor, texCod) * texture(aoMap, texCod).r;
    finalColor = finalBaseColor * lightFactor + specularFactor * (1-texture(roughnessMap, texCod).r) * texture(metalnessMap, texCod).r + finalBaseColor * 0.5;
}