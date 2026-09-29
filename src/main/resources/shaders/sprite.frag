#version 330 core

in vec2 vUV;
out vec4 fragColor;

uniform sampler2D uTexture;
uniform float uAlpha;
uniform vec4 uTint;

void main() {
    vec4 texColor = texture(uTexture, vUV);
    fragColor = texColor * uTint * vec4(1.0, 1.0, 1.0, uAlpha);
}