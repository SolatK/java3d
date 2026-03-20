#version 430 core

layout (location = 0) in vec3 position; // Наш VBO с координатами
layout (location = 1) in vec2 textures; // Наш VBO с текстурами
layout (location = 2) in float matIdIn; // айди материала для цвета
layout (location = 3) in vec3 normal; // VBO с нормалями


uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform vec3 palette[255];

out vec3 fragNormal;
out vec2 texUV;
out vec3 fragPos;
out vec3 vColor;

void main() {
    fragPos = vec3(modelMatrix * vec4(position, 1.0));
    vColor = palette[int(matIdIn + 0.5)];

    gl_Position  = projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1.0);

    fragNormal = normalize(mat3(modelMatrix) * normal);
    texUV = textures;
}