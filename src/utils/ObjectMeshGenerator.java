package utils;

import graphics.Mesh;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL43C.*;

public class ObjectMeshGenerator {


    public static Mesh load(Path path) {
        List<Float> rawVertices = new ArrayList<>();
        List<Float> rawTextures = new ArrayList<>();


        List<Float> combinedData = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        Map<String, Integer> uniqueVertices = new HashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.trim().split("\\s+");
                if (tokens.length < 2) continue;

                switch (tokens[0]) {
                    case "v": // Координаты вершин
                        rawVertices.add(Float.parseFloat(tokens[1]));
                        rawVertices.add(Float.parseFloat(tokens[2]));
                        rawVertices.add(Float.parseFloat(tokens[3]));
                        break;
                    case "vt": // Текстурные координаты
                        rawTextures.add(Float.parseFloat(tokens[1]));
                        rawTextures.add(Float.parseFloat(tokens[2]));
                        break;
                    case "f": // Грани
                        List<Integer> faceIndices = new ArrayList<>();
                        for (int i = 1; i < tokens.length; i++) {
                            String vertexData = tokens[i]; // Формат "v/vt/vn"

                            if (!uniqueVertices.containsKey(vertexData)) {
                                String[] parts = vertexData.split("/");
                                int vIdx = (Integer.parseInt(parts[0]) - 1) * 3;
                                int vtIdx = (Integer.parseInt(parts[1]) - 1) * 2;

                                int newIndex = combinedData.size() / 5; // 5 элементов на вершину (3 поз + 2 текс)

                                // Добавляем позицию
                                combinedData.add(rawVertices.get(vIdx));
                                combinedData.add(rawVertices.get(vIdx + 1));
                                combinedData.add(rawVertices.get(vIdx + 2));
                                // Добавляем текстуру
                                combinedData.add(rawTextures.get(vtIdx));
                                combinedData.add(rawTextures.get(vtIdx + 1));

                                uniqueVertices.put(vertexData, newIndex);
                            }
                            faceIndices.add(uniqueVertices.get(vertexData));
                        }

                        // Триангуляция
                        for (int i = 1; i < faceIndices.size() - 1; i++) {
                            indices.add(faceIndices.get(0));
                            indices.add(faceIndices.get(i));
                            indices.add(faceIndices.get(i + 1));
                        }
                        break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения OBJ: " + path, e);
        }

        float[] fArray = new float[combinedData.size()];
        int i = 0;
        for (Float data: combinedData) {
            fArray[i++] = data;
        }

        return createMesh(
                fArray,
                indices.stream().mapToInt(Integer::intValue).toArray()
        );
    }

    public static Mesh createMesh(float[] posArray, int[] idxArray) {

        float[] normalsArray = calculateNormals(posArray, idxArray);


        //VBO для позиций и текстур
        int stride = 5 * Float.BYTES;
        int vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);
        int vboId = glGenBuffers();
        FloatBuffer posBuffer = MemoryUtil.memAllocFloat(posArray.length);
        posBuffer.put(posArray).flip();

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);

        //данные типа x, y, z, u, v
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);


        //VBO для НОРМАЛЕЙ
        int normalVboId = glGenBuffers();
        FloatBuffer normalBuffer = MemoryUtil.memAllocFloat(normalsArray.length);
        normalBuffer.put(normalsArray).flip();

        glBindBuffer(GL_ARRAY_BUFFER, normalVboId);
        glBufferData(GL_ARRAY_BUFFER, normalBuffer, GL_STATIC_DRAW);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0); // location = 3
        glEnableVertexAttribArray(2);

        //VBO для индексов (EBO)
        int idxVboId = glGenBuffers();
        IntBuffer idxBuffer = MemoryUtil.memAllocInt(idxArray.length);
        idxBuffer.put(idxArray).flip();

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idxVboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, idxBuffer, GL_STATIC_DRAW);

        // Освобождаем память NIO (но не буферы в видеокарте!)
        MemoryUtil.memFree(posBuffer);
        MemoryUtil.memFree(normalBuffer);
        MemoryUtil.memFree(idxBuffer);

        glBindVertexArray(0);

        return new Mesh(vaoId, idxArray.length, vboId, idxVboId, normalVboId);
    }

    public static float[] calculateNormals(float[] vertices, int[] indices) {
        // Количество уникальных вершин
        int vertexCount = vertices.length / 5;
        Vector3f[] vNormals = new Vector3f[vertexCount];
        for (int i = 0; i < vertexCount; i++) vNormals[i] = new Vector3f(0, 0, 0);

        //проходим по всем треугольникам
        for (int i = 0; i < indices.length; i += 3) {
            int i0 = indices[i];
            int i1 = indices[i+1];
            int i2 = indices[i+2];

            // Получаем позиции (извлекаем только XYZ, пропуская UV через * 5)
            Vector3f v0 = new Vector3f(vertices[i0*5], vertices[i0*5+1], vertices[i0*5+2]);
            Vector3f v1 = new Vector3f(vertices[i1*5], vertices[i1*5+1], vertices[i1*5+2]);
            Vector3f v2 = new Vector3f(vertices[i2*5], vertices[i2*5+1], vertices[i2*5+2]);

            //Считаем нормаль грани
            Vector3f edge1 = new Vector3f(v1).sub(v0);
            Vector3f edge2 = new Vector3f(v2).sub(v0);
            Vector3f faceNormal = new Vector3f(edge1).cross(edge2);

            //Суммируем
            vNormals[i0].add(faceNormal);
            vNormals[i1].add(faceNormal);
            vNormals[i2].add(faceNormal);
        }

        // Формируем итоговый массив (строго 3 флоата на вершину)
        float[] normalsArray = new float[vertexCount * 3];
        for (int i = 0; i < vertexCount; i++) {
            vNormals[i].normalize(); // Теперь нормализуем среднее значение
            normalsArray[i*3]     = vNormals[i].x;
            normalsArray[i*3 + 1] = vNormals[i].y;
            normalsArray[i*3 + 2] = vNormals[i].z;
        }
        return normalsArray;
    }

}
