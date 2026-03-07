package core.graphics;

import java.util.Arrays;

public class Matrix4x4 {

    public float[][] m = new float[4][4];

    public Matrix4x4() {
        for (float[] row: m) {
            for (float column: row) {
                column = 0;
            }
        }
    }

    //единичная матрица
    public void makeIdentity() {
        for (int i = 0; i < m.length; i++) {
            m[i][i] = 1f;
        }
    }

    public void makeRotationZ(float angle) {
        m[0][0] = (float) Math.cos(angle);
        m[0][1] = (float) Math.sin(angle);
        m[1][0] = (float) -Math.sin(angle);
        m[1][1] = (float) Math.cos(angle);
        m[2][2] = 1f;
        m[3][3] = 1f;
    }

    public void makeRotationX(float angle) {
        m[0][0] = 1f;
        m[1][1] = (float) Math.cos(angle);
        m[1][2] = (float) Math.sin(angle);
        m[2][1] = (float) -Math.sin(angle);
        m[2][2] = (float) Math.cos(angle);
        m[3][3] = 1f;
    }

    public void makeRotationY(float angle) {
        m[0][0] = (float) Math.cos(angle);
        m[0][2] = (float) Math.sin(angle);
        m[2][0] = (float) -Math.sin(angle);
        m[1][1] = 1f;
        m[2][2] = (float) Math.cos(angle);
        m[3][3] = 1f;
    }

    public void makeTranslation(float x, float y, float z) {
        makeIdentity();
        m[3][0] = x;
        m[3][1] = y;
        m[3][2] = z;
    }

    public void makeProjection(float fov, float aspectRatio, float near, float far) {
        float fovRad = (float) (1.0f / Math.tan(fov * 0.5f / 180.0f * 3.14159f));
        m[0][0] = aspectRatio * fovRad;
        m[1][1] = fovRad;
        m[2][2] = far / (far - near);
        m[3][2] = (-far * near) / (far - near);
        m[2][3] = 1.0f;
        m[3][3] = 0.0f;
    }
}
