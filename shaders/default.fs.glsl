#version 330 core

in vec2 fTex;
in vec3 fNor;
in vec4 fCol;
in vec3 fPos;

uniform vec3 camPos;
uniform vec3 lightF;
uniform sampler2D tex;
uniform sampler2D nor;

out vec4 finalColor;

void main()
{
    vec3 nfNor = normalize( 2 * texture(nor, fTex).rgb - 1 );
    vec3 light = normalize(-lightF);
    vec3 posToCam = normalize(camPos - fPos);

    float lightFactor = max(dot(nfNor, light), 0.2);
    float specularFactor = max(dot(reflect(-light, nfNor), posToCam), 0.0);
    specularFactor = pow(specularFactor, 20);

    vec4 color = vec4(vec3(0.15f, 0.15f, 0.15f), 1.0f);
    if(fCol != vec4(0.0f)) color = fCol;
    finalColor = texture(tex, fTex) * lightFactor + specularFactor;
}