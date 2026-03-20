package utils;

import core.World;
import entity.Chunk;
import org.joml.SimplexNoise;
import org.joml.Vector3f;

import static core.Config.isoLevel;

public class ChunkGenerator {
    public static void fillChunk(Chunk chunk) {
        float terrainFreq = 0.005f; // Чем меньше, тем крупнее горы
        float materialFreq = 0.04f;
        float surfaceLevel = (float) World.CHUNK_SIZE / 2; // Примерный уровень "земли"
        Vector3f position = chunk.getPosition();
        int stride = Chunk.stride;

        for (int z = 0; z < stride; z++) {
            for (int y = 0; y < stride; y++) {
                for (int x = 0; x < stride; x++) {
                    // Мировые координаты
                    float realWorldY = position.y + y;

                    //Координаты ДЛЯ шума (с частотой)
                    float wx = (position.x + x) * terrainFreq;
                    float wy = (position.y + y) * terrainFreq;
                    float wz = (position.z + z) * terrainFreq;

                    // 3D Шум Simplex (-1.0 до 1.0)
                    float noise = SimplexNoise.noise(wx, wy, wz);

                    // Добавляем влияние высоты (градиент)
                    // Чем выше y, тем меньше итоговая плотность
                    float heightOffset = (realWorldY - surfaceLevel) * 0.05f;
                    float densityFloat = noise - heightOffset;

                    // Приводим к 0..255 для нашего формата
                    int density = (int) (densityFloat * isoLevel + isoLevel);

                    density = Math.max(0, Math.min(255, density));

                    // Камень (1), если плотность > densityThreshold, иначе Воздух (0)
                    //todo доработать шум для разных материалов
                    int material = 0; // Воздух по умолчанию
                    if (density > isoLevel) {
                        // Используем второй шум для выбора между Травой (1) и Камнем (2)
                        float mNoise = SimplexNoise.noise(wx * materialFreq, wy * materialFreq, wz * materialFreq);

                        // Если шум > 0.3 и мы глубоко под землей — это камень
                        if (mNoise > 0.3f || realWorldY < surfaceLevel - 10) {
                            material = 2; // Камень
                        } else {
                            material = 1; // Трава
                        }
                    }

                    chunk.setBoth(x, y, z, material, density);
                }
            }
        }
    }

    public static void fillAndGenerate(Chunk chunk) {
        ChunkGenerator.fillChunk(chunk);
        chunk.updateMesh();
    }
}
