#version 430 core

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 normal;


uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

out vec3 fragNormal;
out vec3 fragPos;

void main() {
    vec4 worldPosition = modelMatrix * vec4(position, 1.0);

    fragPos = worldPosition.xyz;

    gl_Position  = projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1.0);

    fragNormal = normalize(mat3(modelMatrix) * normal);
}