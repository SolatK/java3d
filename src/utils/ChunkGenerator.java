package utils;

import entity.Chunk;
import org.joml.SimplexNoise;
import org.joml.Vector3f;

public class ChunkGenerator {
    public static void fillChunk(Chunk chunk) {
        float frequency = 0.1f; // Чем меньше, тем крупнее горы
        float surfaceLevel = (float) Chunk.SIZE / 2; // Примерный уровень "земли"
        Vector3f position = chunk.getPosition();
        int stride = Chunk.stride;

        for (int z = 0; z < stride; z++) {
            for (int y = 0; y < stride; y++) {
                for (int x = 0; x < stride; x++) {
                    // Мировые координаты
                    float realWorldY = position.y + y;

                    //Координаты ДЛЯ шума (с частотой)
                    float wx = (position.x + x) * frequency;
                    float wy = realWorldY * frequency;
                    float wz = (position.z + z) * frequency;

                    // 3D Шум Simplex (-1.0 до 1.0)
                    float noise = SimplexNoise.noise(wx, wy, wz);

                    // Добавляем влияние высоты (градиент)
                    // Чем выше y, тем меньше итоговая плотность
                    float heightOffset = (realWorldY - surfaceLevel) * 0.5f;
                    float densityFloat = noise - heightOffset;

                    // Приводим к 0..255 для нашего формата
                    int density = (int) (densityFloat * 127.5f + 127.5f);

                    density = Math.max(0, Math.min(255, density));

                    // Камень (1), если плотность > 128, иначе Воздух (0)
                    //todo доработать шум для разных материалов
                    int material = (density > 128) ? 1 : 0;

                    chunk.setBoth(x, y, z, material, density);
                }
            }
        }
    }
}
