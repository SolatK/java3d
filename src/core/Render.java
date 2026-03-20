package core;

import entity.Camera;
import entity.Chunk;
import graphics.Mesh;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import shader.ShaderProgram;
import utils.ChunkStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

public class Render {

    private Matrix4f projection;
    private Matrix4f view;

    ShaderProgram terrainShader = new ShaderProgram(
            Files.readString(Path.of("resources/shaders/terrain.vert")),
            Files.readString(Path.of("resources/shaders/terrain.frag"))
    );

    public Render() throws IOException {
    }

    public Matrix4f getProjection() {
        return projection;
    }

    public void setProjection(Matrix4f projection) {
        this.projection = projection;
    }

    //debug
    Chunk chunk;
    {
        chunk = new Chunk(0, 0, 0);
    }
    public void render(Camera camera, List<Object> gameObjects, World world) {
        prepare();
        view = camera.getViewMatrix();

        //дебаг режим отрисовки только вершин для дебага
        //glPolygonMode(GL_FRONT_AND_BACK, GL_POINT);
        //glPointSize(5.0f);
        renderChunks(world.getChunks());



    }

    private void renderChunks(Map<Vector3i, Chunk> chunks) {
        //todo вынести всё это в рендер чанкс
        terrainShader.bind();
        terrainShader.setUniform("projectionMatrix", projection);
        terrainShader.setUniform("viewMatrix", view);

        //todo луп по всем чанкам
        for (Chunk chunk: chunks.values()) {
            //пропускаем неготовый чанк
            if (chunk.getStatus() != ChunkStatus.READY && chunk.getStatus() != ChunkStatus.REGENERATING_MESH ) continue;
            Matrix4f model = chunk.getModelMatrix();
            model.identity().translate(chunk.getPosition());
            //оптимизировать чтобы не считать матрицу модели каждый кадр
            terrainShader.setUniform("modelMatrix", chunk.getModelMatrix());
            Mesh mesh = chunk.getMesh();
            glBindVertexArray(mesh.vaoId());

            glDrawElements(GL_TRIANGLES, mesh.vertexCount(), GL_UNSIGNED_INT, 0);

        }

        glBindVertexArray(0);
        terrainShader.unbind();
    }

    private void prepare() {
        // Очистка буферов цвета и глубины
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }


}
