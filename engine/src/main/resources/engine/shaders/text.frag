#version 330 core
in vec2 vUV;
out vec4 fragColor;

uniform sampler2D uTexture;
uniform vec4 uColor;
uniform vec4 uOutlineColor;
uniform vec2 uOutlineStep;

void main() {
    float alpha = texture(uTexture, vUV).r;
    float outline = alpha;
    for (int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
            outline = max(outline, texture(uTexture, vUV + vec2(x, y) * uOutlineStep).r);
        }
    }

    float fill = uColor.a * alpha;
    float edge = uOutlineColor.a * outline * (1.0 - fill);
    float coverage = fill + edge;
    vec3 color = coverage > 0.0 ? (uColor.rgb * fill + uOutlineColor.rgb * edge) / coverage : uColor.rgb;
    fragColor = vec4(color, coverage);
}