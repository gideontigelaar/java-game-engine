#version 330 core

in vec2 vUV;
out vec4 fragColor;

uniform float iTime;
uniform vec2 iResolution;
uniform int palette;
uniform int previousPalette;
uniform float startTime;
uniform vec2 mousePosition;

const vec3 PALETTES[9] = vec3[9](
    vec3(1, 0.149, 0.455), vec3(1, 0.502, 0.643), vec3(0.58, 0.129, 0.416),
    vec3(0.063, 0.824, 0.459), vec3(0.749, 1, 0.235), vec3(0, 0.471, 0.6),
    vec3(0.149, 0.455, 1), vec3(0.502, 0.643, 1), vec3(0.129, 0.267, 0.58)
);

void main()
{
    vec2 fragCoord = vec2(vUV.x * iResolution.x, vUV.y * iResolution.y);
    float blockPx = 4.0;
    float GroupSize = 20.0;
    float amp = 20.0;
    float freq = 0.01;
    float speed = 15.0;
    float waveSpeed = 2.0;

    float circleSpeed = 15.0;

    vec2 transitionPosition = mousePosition;
    float radius = exp(min((iTime - startTime) * circleSpeed, 20.0));

    vec2 b = floor(fragCoord / blockPx);

    float wave = sin((b.x + 0.5) * blockPx * freq + iTime * waveSpeed) * amp;
    b.y += floor(wave / blockPx + 0.5);

    vec2 cell = b + floor(iTime * speed);

    vec2 snapped = (floor(fragCoord / (blockPx * 2.0)) + 0.5) * (blockPx * 2.0);

    vec2 group = floor(cell / GroupSize);
    vec2 local = mod(cell, GroupSize);
    bool inner = local.x > 1.0 && local.x < 18.0 && local.y > 1.0 && local.y < 18.0;

    int currentPalette = distance(snapped, transitionPosition) < radius ? palette : previousPalette;

    int shade;
    if (mod(group.x + group.y, 2.0) == 0.0)
    {
        shade = inner ? 0 : 1;
    }
    else
    {
        shade = inner ? 1 : 2;
    }

    fragColor = vec4(PALETTES[currentPalette * 3 + shade], 1.0);
}