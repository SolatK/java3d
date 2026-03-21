package utils;

import core.World;
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

import static core.Config.isoLevel;
import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.*;

public class TerrainMeshGenerator {
    public static MeshData generateChunkMeshData(Chunk chunk, World world) {
        List<Float> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        List<Integer> materials = new ArrayList<>();
        int vertexCount = 0;
        int size = World.CHUNK_SIZE;

        for (int z = 0; z < size; z++) {
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {

                    int[] cubeValues = new int[8];

                    for (int i = 0; i < 8; i++) {
                        Vector3i offset = CubeData.OFFSETS[i];
                        //небольшой костыль. При первой генерации мы берем информацию из самого чанка
                        // с учетом stride, а при изменении уже из соседнего чанка, это гарантирует что он сгенерирован
                        if (chunk.getStatus().code >= ChunkStatus.REGENERATING_MESH.code) {
                            cubeValues[i] = chunk.getDensityGlobal(x + offset.x, y + offset.y, z + offset.z, world);
                        } else {
                            cubeValues[i] = chunk.getDensity(x + offset.x, y + offset.y, z + offset.z);
                        }
                    }

                    int cubeIndex = 0;
                    if (cubeValues[0] > isoLevel) cubeIndex |= 1;
                    if (cubeValues[1] > isoLevel) cubeIndex |= 2;
                    if (cubeValues[2] > isoLevel) cubeIndex |= 4;
                    if (cubeValues[3] > isoLevel) cubeIndex |= 8;
                    if (cubeValues[4] > isoLevel) cubeIndex |= 16;
                    if (cubeValues[5] > isoLevel) cubeIndex |= 32;
                    if (cubeValues[6] > isoLevel) cubeIndex |= 64;
                    if (cubeValues[7] > isoLevel) cubeIndex |= 128;

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

                            // --- ЛОГИКА МАТЕРИАЛА ---
                            // Узнаем, какие два угла (0-7) соединяет текущее ребро
                            int v1Idx = CubeData.EDGE_CONNECTIONS[edgeIdx][0];
                            int v2Idx = CubeData.EDGE_CONNECTIONS[edgeIdx][1];

                            // Получаем их мировые (относительно чанка) координаты
                            Vector3i o1 = CubeData.OFFSETS[v1Idx];
                            Vector3i o2 = CubeData.OFFSETS[v2Idx];

                            // Выбираем материал того вокселя, у которого плотность выше порога
                            int mat;
                            if (cubeValues[v1Idx] > isoLevel) {
                                mat = chunk.getMaterial(x + o1.x, y + o1.y, z + o1.z);
                            } else {
                                mat = chunk.getMaterial(x + o2.x, y + o2.y, z + o2.z);
                            }

                            // Добавляем материал для КАЖДОЙ вершины
                            materials.add(mat);

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

        int[] materialsArray = new int[materials.size()];
        for (int i = 0; i < materials.size(); i++) {
            materialsArray[i] = materials.get(i);
        }

        return new MeshData(verticesArray, normals, indicesArray, materialsArray);
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
    private static Vector3f interpolateEdge(int edgeIdx, int x, int y, int z, int[] v) {
        // Получаем индексы углов, которые соединяет это ребро
        int v1Idx = CubeData.EDGE_CONNECTIONS[edgeIdx][0];
        int v2Idx = CubeData.EDGE_CONNECTIONS[edgeIdx][1];

        float val1 = v[v1Idx];
        float val2 = v[v2Idx];

        // Интерполяция: (ISO - V1) / (V2 - V1)
        float t = (isoLevel - val1) / (val2 - val1);

        Vector3f p1 = new Vector3f(x, y, z).add(new Vector3f(CubeData.OFFSETS[v1Idx]));
        Vector3f p2 = new Vector3f(x, y, z).add(new Vector3f(CubeData.OFFSETS[v2Idx]));

        return p1.lerp(p2, t);
    }

    //для чанка
    public static Mesh uploadMesh(MeshData data) {
        int vaoId = glGenVertexArrays();

        int idxVboId = glGenBuffers();
        IntBuffer idxBuffer = MemoryUtil.memAllocInt(data.indices().length);

        int vboIdVertex = glGenBuffers();
        FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(data.vertices().length);

        int vboIdNormal = glGenBuffers();
        FloatBuffer normalBuffer = MemoryUtil.memAllocFloat(data.normals().length);

        int vboIdMaterial = glGenBuffers();
        IntBuffer materialBuffer = MemoryUtil.memAllocInt(data.materials().length);

        try {

            glBindVertexArray(vaoId);

            //Индексы (EBO)
            idxBuffer.put(data.indices()).flip();

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idxVboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, idxBuffer, GL_STATIC_DRAW);


            //vbo для вершин
            vertexBuffer.put(data.vertices()).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboIdVertex);
            glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(0);

            //vbo Для нормалей
            normalBuffer.put(data.normals()).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboIdNormal);
            glBufferData(GL_ARRAY_BUFFER, normalBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(1);

            //vbo для материалов
            materialBuffer.put(data.materials()).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboIdMaterial);
            glBufferData(GL_ARRAY_BUFFER, materialBuffer, GL_STATIC_DRAW);
            glVertexAttribIPointer (2, 1, GL_INT, 0, 0);
            glEnableVertexAttribArray(2);

            glBindBuffer(GL_ARRAY_BUFFER, 0);
        } finally {
            // Освобождаем память NIO (но не буферы в видеокарте!)
            MemoryUtil.memFree(vertexBuffer);
            MemoryUtil.memFree(normalBuffer);
            MemoryUtil.memFree(idxBuffer);
            MemoryUtil.memFree(materialBuffer);
        }

        glBindVertexArray(0);

        return new Mesh(vaoId, data.indices().length, idxVboId, vboIdVertex, vboIdNormal, vboIdMaterial);
    }
}
