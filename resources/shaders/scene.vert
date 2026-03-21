#version 430 core

layout (location = 0) in vec3 aPos;    // Позиция вершины
layout (location = 2) in vec3 aNormal; // Вектор нормали

out vec3 vNormal; // Передаем во фрагментный шейдер

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

void main() {
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(aPos, 1.0);

    // Передаем нормаль (в мировых координатах или просто локальных)
    // В идеале: mat3(transpose(inverse(model))) * aNormal
    vNormal = aNormal;
}