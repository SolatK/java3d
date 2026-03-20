package entity;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    private Vector3f position;
    private Vector3f front;
    private Vector3f up;

    private float yaw = -90.0f; // Поворот влево/вправо
    private float pitch = 0.0f; // Поворот вверх/вниз

    public Camera(Vector3f startPos) {
        this.position = startPos;
        this.front = new Vector3f(0, 0, -1);
        this.up = new Vector3f(0, 1, 0);
    }

    public void updateVectors() {
        Vector3f newFront = new Vector3f();
        newFront.x = (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        newFront.y = (float) Math.sin(Math.toRadians(pitch));
        newFront.z = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        this.front = newFront.normalize();
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f().lookAt(position, new Vector3f(position).add(front), up);
    }

    public void addYaw(float offset) {
        //todo остаток от деления?
        this.yaw += offset;
    }
    public void addPitch(float offset) {
        this.pitch += offset;
        if (this.pitch > 89.0f) this.pitch = 89.0f;
        if (this.pitch < -89.0f) this.pitch = -89.0f;
    }

    public Vector3f getPos() {
        return position;
    }

    public Vector3f getFront() {
        return front;
    }

    public Vector3f getUp() {
        return up;
    }
}
