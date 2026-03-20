package utils;

import entity.Chunk;
import graphics.Mesh;
import graphics.MeshData;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;

public class MeshGenerator {
    public static MeshData generateChunkMeshData(Chunk chunk) {
        List<Float> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        int vertexCount = 0;

        for (int z = 0; z < Chunk.SIZE; z++) {
            for (int y = 0; y < Chunk.SIZE; y++) {
                for (int x = 0; x < Chunk.SIZE; x++) {
                    // 1. Собираем плотность в 8 углах текущего куба
                    float[] cubeValues = new float[8];
                    for (int i = 0; i < 8; i++) {
                        Vector3i offset = CubeData.OFFSETS[i];
                        cubeValues[i] = chunk.getDensity(x + offset.x, y + offset.y, z + offset.z);
                    }

                    // 2. Определяем индекс конфигурации (от 0 до 255)
                    int cubeIndex = 0;
                    if (cubeValues[0] > 128) cubeIndex |= 1;
                    if (cubeValues[1] > 128) cubeIndex |= 2;
                    if (cubeValues[2] > 128) cubeIndex |= 4;
                    if (cubeValues[3] > 128) cubeIndex |= 8;
                    if (cubeValues[4] > 128) cubeIndex |= 16;
                    if (cubeValues[5] > 128) cubeIndex |= 32;
                    if (cubeValues[6] > 128) cubeIndex |= 64;
                    if (cubeValues[7] > 128) cubeIndex |= 128;

                    // 3. Генерируем треугольники по таблице
                    int[] edges = TriTable.TRIANGLE_TABLE[cubeIndex];

                    // Если массив пустой (как в первом элементе вашей таблицы {}), пропускаем куб
                    if (edges.length == 0) continue;

                    for (int i = 0; i < edges.length; i += 3) {
                        // Создаем 3 вершины треугольника
                        for (int j = 0; j < 3; j++) {
                            int edgeIdx = edges[i + j];
                            Vector3f pos = interpolateEdge(edgeIdx, x, y, z, cubeValues);

                            vertices.add(pos.x);
                            vertices.add(pos.y);
                            vertices.add(pos.z);

                            // Добавляем нормали (пока можно заглушку или рассчитать по градиенту плотности)
                            indices.add(vertexCount++);
                        }
                    }
                }
            }
        }
        float[] normals = calculateNormals(vertices, indices);

        float[] verticesArray = new float[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) {
            verticesArray[i] = vertices.get(i);
        }

        int[] indicesArray = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indicesArray[i] = indices.get(i);
        }

        return new MeshData(verticesArray, normals, indicesArray);
    }

    public static float[] calculateNormals(List<Float> vertices, List<Integer> indices) {
        // Количество уникальных вершин
        int vertexCount = vertices.size() / 3;
        Vector3f[] vNormals = new Vector3f[vertexCount];
        for (int i = 0; i < vertexCount; i++) vNormals[i] = new Vector3f(0, 0, 0);

        for (int i = 0; i < indices.size(); i += 3) {
            int i0 = indices.get(i);
            int i1 = indices.get(i + 1);
            int i2 = indices.get(i + 2);

            // Получаем позиции
            Vector3f v0 = new Vector3f(vertices.get(i0*3), vertices.get(i0*3 + 1), vertices.get(i0*3 + 2));
            Vector3f v1 = new Vector3f(vertices.get(i1*3), vertices.get(i1*3 + 1), vertices.get(i1*3 + 2));
            Vector3f v2 = new Vector3f(vertices.get(i2*3), vertices.get(i2*3 + 1), vertices.get(i2*3 + 2));

            //Считаем нормаль грани
            Vector3f edge1 = new Vector3f(v1).sub(v0);
            Vector3f edge2 = new Vector3f(v2).sub(v0);
            Vector3f faceNormal = new Vector3f(edge1).cross(edge2); // Не нормализуйте здесь, чтобы учесть площадь!

            //Суммируем
            vNormals[i0].add(faceNormal);
            vNormals[i1].add(faceNormal);
            vNormals[i2].add(faceNormal);
        }

        float[] normalsArray = new float[vertexCount * 3];
        for (int i = 0; i < vertexCount; i++) {
            vNormals[i].normalize(); // Теперь нормализуем среднее значение
            normalsArray[i*3]     = vNormals[i].x;
            normalsArray[i*3 + 1] = vNormals[i].y;
            normalsArray[i*3 + 2] = vNormals[i].z;
        }
        return normalsArray;
    }
    private static Vector3f interpolateEdge(int edgeIdx, int x, int y, int z, float[] v) {
        // Получаем индексы углов, которые соединяет это ребро
        int v1Idx = CubeData.EDGE_CONNECTIONS[edgeIdx][0];
        int v2Idx = CubeData.EDGE_CONNECTIONS[edgeIdx][1];

        float val1 = v[v1Idx];
        float val2 = v[v2Idx];

        // Интерполяция: (ISO - V1) / (V2 - V1)
        float t = (128f - val1) / (val2 - val1);

        Vector3f p1 = new Vector3f(x, y, z).add(new Vector3f(CubeData.OFFSETS[v1Idx]));
        Vector3f p2 = new Vector3f(x, y, z).add(new Vector3f(CubeData.OFFSETS[v2Idx]));

        return p1.lerp(p2, t);
    }

    //для чанка
    public static Mesh generateMesh(MeshData data) {
        int vaoId = glGenVertexArrays();

        IntBuffer idxBuffer = MemoryUtil.memAllocInt(data.indices().length);

        FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(data.vertices().length);

        FloatBuffer normalBuffer = MemoryUtil.memAllocFloat(data.normals().length);

        try {

            glBindVertexArray(vaoId);

            //Индексы (EBO)
            int idxVboId = glGenBuffers();

            idxBuffer.put(data.indices()).flip();

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idxVboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, idxBuffer, GL_STATIC_DRAW);


            //vbo для вершин
            int vboIdVertex = glGenBuffers();

            vertexBuffer.put(data.vertices()).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboIdVertex);
            glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(0);

            //vbo Для нормалей
            int vboIdNormal = glGenBuffers();

            normalBuffer.put(data.normals()).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboIdNormal);
            glBufferData(GL_ARRAY_BUFFER, normalBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(1);

            glBindBuffer(GL_ARRAY_BUFFER, 0);
        } finally {
            // Освобождаем память NIO (но не буферы в видеокарте!)
            MemoryUtil.memFree(vertexBuffer);
            MemoryUtil.memFree(normalBuffer);
            MemoryUtil.memFree(idxBuffer);
        }

        glBindVertexArray(0);

        return new Mesh(vaoId, data.indices().length);
    }
}
