#version 330 core
layout (location = 0) in vec4 aPosTex;
out vec2 vUV;

uniform mat4 uProjection;

void main() {
    vUV = aPosTex.zw;
    gl_Position = uProjection * vec4(aPosTex.xy, 0.0, 1.0);
}