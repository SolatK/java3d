package entity;

import org.joml.Vector3f;

public class Entity extends Renderable{
    private Vector3f rotation = new Vector3f(0, 0, 0);
    private float scale = 1f;

    public Entity(Vector3f pos) {
        position = pos;
    }

    @Override
    public void updateModelMatrix() {
        modelMatrix.identity()
                .translate(position)
                .rotateX((float)Math.toRadians(rotation.x))
                .rotateY((float)Math.toRadians(rotation.y))
                .rotateZ((float)Math.toRadians(rotation.z))
                .scale(scale);
    }
}
