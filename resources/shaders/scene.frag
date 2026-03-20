#version 430 core

in vec2 texUV;
in vec3 fragNormal;
in vec3 fragPos;
in vec3 vColor;
out vec4 fragColor;

uniform sampler2D textureSampler;
uniform vec3 viewPos; // Позиция камеры

void main() {
    // 1. Вектор от точки объекта к камере
    //todo сделать нормальное освещение
    //vec3 lightVec = viewPos - fragPos; направленный свет "фонарик"
    vec3 lightVec = vec3(1, 1, 0); //примитивный свет
    float distance = length(lightVec); // Расстояние до камеры
    vec3 lightDir = normalize(lightVec);

    // 2. Освещение (Diffuse)
    vec3 norm = normalize(fragNormal);
    float diff = max(dot(norm, lightDir), 0.0);

    // 3. Затухание (Attenuation)
    // Эти числа можно менять:
    // constant (обычно 1.0), linear (линейное), quadratic (квадратичное)
    float constant = 1.0;
    //float linear = 0.0025;    // Чем выше, тем быстрее гаснет свет
    //float quadratic = 0.005; // Влияет на резкость затухания вдали
    float linear = 0.05;    // Чем выше, тем быстрее гаснет свет
    float quadratic = 0.05; // Влияет на резкость затухания вдали

    float attenuation = 1.0 / (constant + linear * distance + quadratic * (distance * distance));

    // 4. Итоговая яркость
    float ambient = 0.2;
    // Ослабляем только прямой свет (diff), фоновый свет (ambient) остается всегда
    float brightness = ambient + (diff * attenuation);

    // 5. Цвет и текстура
    vec4 texColor = texture(textureSampler, texUV);
    fragColor = vec4(vColor * brightness, 1);
    //fragColor = vec4(fragNormal, 1);
}