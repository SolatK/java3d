package graphics;

import static org.lwjgl.opengl.GL30C.glDeleteVertexArrays;

public record Mesh(int vaoId, int vertexCount) {
    // Можно добавить метод очистки прямо в record
    public void cleanup() {
        glDeleteVertexArrays(vaoId);
    }
}
