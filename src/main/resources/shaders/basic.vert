#version 330 core

layout (location = 0) in vec2 aPosition;
layout (location = 1) in vec3 aColor;

uniform mat4 uMVP;

out vec3 vColor;

void main() {
    gl_Position = uMVP * vec4(aPosition, 0.0, 1.0);
    vColor = aColor;
}