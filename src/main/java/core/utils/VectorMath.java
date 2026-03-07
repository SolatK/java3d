package core.utils;

import core.graphics.Matrix4x4;
import core.graphics.Vec3d;

public class VectorMath {
    public static Vec3d vectorAdd(Vec3d a, Vec3d b) {
        return new Vec3d(a.x + b.x, a.y + b.y, a.z + b.z);
    }

    public static Vec3d vectorSubtract(Vec3d a, Vec3d b) {
        return new Vec3d(a.x - b.x, a.y - b.y, a.z - b.z);
    }

    public static Vec3d vectorMultiply(Vec3d a, float b) {
        return new Vec3d(a.x * b, a.y * b, a.z * b);
    }

    public static Vec3d vectorDivide(Vec3d a, float b) {
        return new Vec3d(a.x / b, a.y / b, a.z / b);
    }

    public static float vectorDotProduct(Vec3d vecA, Vec3d vecB) {
        return vecA.x * vecB.x + vecA.y * vecB.y + vecA.z * vecB.z;
    }
    public static float vectorLength(Vec3d vec) {
        return (float) Math.sqrt(vec.x*vec.x + vec.y * vec.y + vec.z * vec.z);
    }

    public static Vec3d vectorNormalize(Vec3d vec) {
        float l = vectorLength(vec);
        return vectorDivide(vec, l);
    }

    public static Vec3d vectorCrossProduct(Vec3d a, Vec3d b) {
        return new Vec3d(
        a.y * b.z - a.z * b.y,
        a.z * b.x - a.x * b.z,
        a.x * b.y - a.y * b.x
        );
    }

    public static Vec3d matrixMultiplyVector(Matrix4x4 m, Vec3d vec) {
        //TODO сунуть данные сразу в конструктор
        Vec3d newVec = new Vec3d();

        newVec.x = vec.x * m.m[0][0] + vec.y * m.m[1][0] + vec.z * m.m[2][0] + vec.w * m.m[3][0];
        newVec.y = vec.x * m.m[0][1] + vec.y * m.m[1][1] + vec.z * m.m[2][1] + vec.w * m.m[3][1];
        newVec.z = vec.x * m.m[0][2] + vec.y * m.m[1][2] + vec.z * m.m[2][2] + vec.w * m.m[3][2];
        newVec.w = vec.x * m.m[0][3] + vec.y * m.m[1][3] + vec.z * m.m[2][3] + vec.w * m.m[3][3];
        return newVec;
    }

    public static Matrix4x4 matrixMultiply(Matrix4x4 a, Matrix4x4 b) {
        Matrix4x4 mat = new Matrix4x4();
        for (int c = 0; c < 4; c++) {
            for (int r = 0; r < 4; r++) {
                mat.m[r][c] = a.m[r][0] * b.m[0][c] + a.m[r][1] * b.m[1][c] + a.m[r][2] * b.m[2][c] + a.m[r][3] * b.m[3][c];
            }
        }
        return mat;
    }
}
