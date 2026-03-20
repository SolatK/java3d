#version 430 core

in vec3 fragNormal;
in vec3 fragPos;

out vec4 out_Color;

void main() {
    // 1. Нормализуем входящий вектор (интерполяция может его немного исказить)
    vec3 normal = normalize(fragNormal);

    // 2. Преобразуем диапазон нормали из [-1, 1] в [0, 1]
    // Чтобы отрицательные направления (например, вниз или влево) не были просто черными
    vec3 debugColor = normal * 0.5 + 0.5;

    // 3. Выводим результат (альфа-канал всегда 1.0 — непрозрачный)
    out_Color = vec4(debugColor, 1.0);
}