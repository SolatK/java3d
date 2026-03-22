package graphics;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15C.glGenBuffers;

public class MeshData {
    IntBuffer idxBuffer;

    FloatBuffer vertexBuffer;

    FloatBuffer normalBuffer;

    IntBuffer materialBuffer;

    int indexCount = 0;
    public MeshData(float[] vertices, float[] normals, int[] indices, int[] materials) {

        //заполняем буфер тут, чтобы в основном потоке сразу передать его в видеокарту
        idxBuffer = MemoryUtil.memAllocInt(indices.length).put(indices).flip();
        vertexBuffer = MemoryUtil.memAllocFloat(vertices.length).put(vertices).flip();
        normalBuffer = MemoryUtil.memAllocFloat(normals.length).put(normals).flip();
        materialBuffer = MemoryUtil.memAllocInt(materials.length).put(materials).flip();

        indexCount = indices.length;

    }

    public IntBuffer getIdxBuffer() {
        return idxBuffer;
    }

    public FloatBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public FloatBuffer getNormalBuffer() {
        return normalBuffer;
    }

    public IntBuffer getMaterialBuffer() {
        return materialBuffer;
    }

    public int getIndexCount() {
        return indexCount;
    }

    public void free() {
        MemoryUtil.memFree(vertexBuffer);
        MemoryUtil.memFree(normalBuffer);
        MemoryUtil.memFree(idxBuffer);
        MemoryUtil.memFree(materialBuffer);
    }
}
