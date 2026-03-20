#version 430 core

in vec3 fragNormal;
in vec3 fragPos;
flat in int fragMat;

out vec4 out_Color;

void main() {
    // 1. Базовый цвет
    vec3 baseColor;

    if (fragMat == 1) {
        baseColor = vec3(0.2, 0.6, 0.1); // Трава (Зеленый)
    } else if (fragMat == 2) {
        baseColor = vec3(0.4, 0.4, 0.4); // Камень (Серый)
    } else {
        baseColor = vec3(0.8, 0.0, 0.8); // Ошибка (Розовый)
    }

    // 2. Направление "солнца" (светит немного сбоку и сверху)
    vec3 lightDir = normalize(vec3(0.5, 1.0, 0.3));

    // 3. Расчет диффузного освещения (скалярное произведение)
    // Чем перпендикулярнее свет к нормали, тем ярче точка
    float diff = max(dot(normalize(fragNormal), lightDir), 0.0);

    // 4. Эмбиент (фоновый свет), чтобы тени не были абсолютно черными
    float ambient = 0.2;
    float finalLight = diff + ambient;

    out_Color = vec4(baseColor * finalLight, 1.0);
}