package entity;

import graphics.Mesh;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public abstract class Renderable {
    protected Vector3f position = new Vector3f(0, 0, 0);
    protected Matrix4f modelMatrix = new Matrix4f();
    protected Mesh mesh; //VAO/VBO

    public abstract void updateModelMatrix();

    public Mesh getMesh() { return mesh; }

    public void setMesh(Mesh mesh) { this.mesh = mesh; }

    public Matrix4f getModelMatrix() { return modelMatrix; }

    public Vector3f getPosition() {
        return position;
    }
}
