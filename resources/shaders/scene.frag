#version 430 core

in vec3 vNormal;
out vec4 FragColor;

void main() {
    // Нормализуем на всякий случай после интерполяции
    vec3 n = normalize(vNormal);

    // Преобразуем диапазон [-1, 1] в [0, 1]
    // x -> Red, y -> Green, z -> Blue
    vec3 rgb = n * 0.5 + 0.5;

    FragColor = vec4(rgb, 1.0);
}