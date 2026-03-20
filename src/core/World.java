package core;

import entity.Chunk;
import org.joml.Math;
import org.joml.Vector3f;
import org.joml.Vector3i;
import utils.ChunkGenerator;
import utils.ChunkStatus;
import utils.MeshGenerator;
import utils.RaycastResult;

import java.util.*;
import java.util.concurrent.*;

import static core.Config.chunkPerFrame;
import static core.Config.isoLevel;

public class World {
    private final Map<Vector3i, Chunk> chunks = new ConcurrentHashMap<>();
    private final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
    private final Queue<Chunk> readyToUpload = new ConcurrentLinkedQueue<>();
    private final Set<Chunk> dirtyChunks = Collections.synchronizedSet(new LinkedHashSet<>());

    public static final int CHUNK_SIZE = 32;

    public void requestChunk(int cx, int cy, int cz) {
        Vector3i key = new Vector3i(cx, cy, cz);
        Chunk chunk = chunks.computeIfAbsent(key, k -> new Chunk(cx, cy, cz));

        if (chunk.getStatus() == ChunkStatus.EMPTY) {
            chunk.setStatus(ChunkStatus.GENERATING_BLOCKS);
            submitChunk(chunk);
        }
    }

    private void submitChunk(Chunk chunk) {
        threadPool.submit(() -> generateChunk(chunk));
    }


    private void generateChunk(Chunk chunk) {
        if (chunk.getStatus() == ChunkStatus.GENERATING_BLOCKS) {
            ChunkGenerator.fillChunk(chunk);
            chunk.setStatus(ChunkStatus.BLOCKS_READY);
        }

        if (chunk.getStatus() == ChunkStatus.BLOCKS_READY) {
            chunk.setMeshData(MeshGenerator.generateChunkMeshData(chunk, this));
            chunk.setStatus(ChunkStatus.GENERATING_MESH);
        }

        if (chunk.getStatus() == ChunkStatus.REGENERATING_MESH) {
            chunk.setMeshData(MeshGenerator.generateChunkMeshData(chunk, this));
        }

        readyToUpload.add(chunk);
    }

    public void setDensity(float wx, float wy, float wz, int value) {
        // 1. Определяем, в каком чанке находится точка
        int cx = (int) Math.floor(wx / CHUNK_SIZE);
        int cy = (int) Math.floor(wy / CHUNK_SIZE);
        int cz = (int) Math.floor(wz / CHUNK_SIZE);

        Chunk chunk = chunks.get(new Vector3i(cx, cy, cz));
        if (chunk != null) {
            // 2. Переводим мировые координаты в локальные (0..16)
            int lx = (int) (wx - (cx * CHUNK_SIZE));
            int ly = (int) (wy - (cy * CHUNK_SIZE));
            int lz = (int) (wz - (cz * CHUNK_SIZE));

            chunk.setDensity(lx, ly, lz, value);
            markDirty(chunk); // Флаг, что меш надо перестроить
        }
    }

    private void markDirty(Chunk chunk) {
        if (chunk != null) {
            dirtyChunks.add(chunk);
        }
    }

    public int getDensity(float wx, float wy, float wz) {

        int cx = (int) Math.floor(wx / CHUNK_SIZE);
        int cy = (int) Math.floor(wy / CHUNK_SIZE);
        int cz = (int) Math.floor(wz / CHUNK_SIZE);

        Chunk chunk = chunks.get(new Vector3i(cx, cy, cz));



        //если чанк есть и сгенерирован
        if (chunk != null && chunk.getStatus().code >= ChunkStatus.BLOCKS_READY.code) {
            int lx = (int) (wx - (cx * CHUNK_SIZE));
            int ly = (int) (wy - (cy * CHUNK_SIZE));
            int lz = (int) (wz - (cz * CHUNK_SIZE));

            return chunk.getDensity(lx, ly, lz);
        }

        // Если чанка нет, считаем, что там пустота (воздух)
        return 0;
    }

    public void modifyTerrain(Vector3f center, float radius, int amount) {
        // 1. Определяем область поиска (Bounding Box) вокруг клика
        int minX = (int) Math.floor(center.x - radius);
        int maxX = (int) Math.ceil(center.x + radius);
        int minY = (int) Math.floor(center.y - radius);
        int maxY = (int) Math.ceil(center.y + radius);
        int minZ = (int) Math.floor(center.z - radius);
        int maxZ = (int) Math.ceil(center.z + radius);

        // 2. Проходим по всем вокселям в этом кубе
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {

                    // 3. Проверяем, входит ли точка в СФЕРУ (дистанция до центра)
                    double dist = center.distance(x, y, z);
                    if (dist <= radius) {
                        // Рассчитываем новую плотность для этой точки
                        int currentDensity = getDensity(x, y, z); // Тебе понадобится этот геттер в World
                        int newDensity = Math.max(0, Math.min(255, currentDensity + amount));

                        // 4. Вызываем твой атомарный метод для каждой точки
                        setDensity(x, y, z, newDensity);
                    }
                }
            }
        }
    }

    public RaycastResult raycast(Vector3f origin, Vector3f direction, float maxDistance) {
        float step = 0.1f; // Точность шага. Чем меньше, тем точнее, но медленнее.
        Vector3f currentPos = new Vector3f(origin);
        Vector3f dirStep = new Vector3f(direction).mul(step);

        for (float distance = 0; distance < maxDistance; distance += step) {
            currentPos.add(dirStep);

            // 1. Узнаем индексы чанка для этой точки
            int cx = (int) Math.floor(currentPos.x / CHUNK_SIZE);
            int cy = (int) Math.floor(currentPos.y / CHUNK_SIZE);
            int cz = (int) Math.floor(currentPos.z / CHUNK_SIZE);

            Vector3i chunkKey = new Vector3i(cx, cy, cz);
            Chunk chunk = chunks.get(chunkKey);

            if (chunk != null) {
                // 2. Переводим в локальные координаты чанка
                int lx = (int) (currentPos.x - (cx * CHUNK_SIZE));
                int ly = (int) (currentPos.y - (cy * CHUNK_SIZE));
                int lz = (int) (currentPos.z - (cz * CHUNK_SIZE));

                // Проверяем плотность (Marching Cubes порог = 128)
                if (chunk.getDensity(lx, ly, lz) > isoLevel) {
                    return new RaycastResult(new Vector3f(currentPos), chunkKey, chunk);
                }
            }
        }
        return null; // Ничего не нашли
    }


    public Map<Vector3i, Chunk> getChunks() {
        return chunks;
    }

    public void updateDirtyChunks() {
        if (dirtyChunks.isEmpty()) return;

        // Ограничиваем количество пересборок мешей за 1 кадр (например, 2-3 чанка)
        // Это предотвращает резкое падение FPS (лаги)
        int updatesThisFrame = 0;

        Iterator<Chunk> iterator = dirtyChunks.iterator();
        while (iterator.hasNext() && updatesThisFrame < chunkPerFrame) {
            Chunk chunk = iterator.next();
            chunk.setStatus(ChunkStatus.REGENERATING_MESH);
            generateChunk(chunk);
            iterator.remove();
            updatesThisFrame++;
        }
    }

    public Queue<Chunk> getReadyToUpload() {
        return readyToUpload;
    }

    public void destroy() {
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                threadPool.shutdownNow(); // Принудительно прерывает выполнение
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
        }
    }
}
