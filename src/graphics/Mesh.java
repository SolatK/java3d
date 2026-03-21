package graphics;

import static org.lwjgl.opengl.GL15C.glDeleteBuffers;
import static org.lwjgl.opengl.GL30C.glDeleteVertexArrays;

public record Mesh(int vaoId, int vertexCount, int... buffers) {

    public void cleanup() {
        glDeleteVertexArrays(vaoId);

        if (buffers != null) {
            glDeleteBuffers(buffers);
        }
    }
}
