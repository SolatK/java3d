package core.utils;

import core.graphics.Matrix4x4;
import core.graphics.Polygon;
import core.graphics.Vec3d;

import java.util.function.BiFunction;
import java.util.function.Function;

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

    public static Matrix4x4 matrixQuickInverse(Matrix4x4 m) // Only for Rotation/Translation Matrices
    {
        Matrix4x4 matrix = new Matrix4x4();
        matrix.m[0][0] = m.m[0][0]; matrix.m[0][1] = m.m[1][0]; matrix.m[0][2] = m.m[2][0]; matrix.m[0][3] = 0.0f;
        matrix.m[1][0] = m.m[0][1]; matrix.m[1][1] = m.m[1][1]; matrix.m[1][2] = m.m[2][1]; matrix.m[1][3] = 0.0f;
        matrix.m[2][0] = m.m[0][2]; matrix.m[2][1] = m.m[1][2]; matrix.m[2][2] = m.m[2][2]; matrix.m[2][3] = 0.0f;
        matrix.m[3][0] = -(m.m[3][0] * matrix.m[0][0] + m.m[3][1] * matrix.m[1][0] + m.m[3][2] * matrix.m[2][0]);
        matrix.m[3][1] = -(m.m[3][0] * matrix.m[0][1] + m.m[3][1] * matrix.m[1][1] + m.m[3][2] * matrix.m[2][1]);
        matrix.m[3][2] = -(m.m[3][0] * matrix.m[0][2] + m.m[3][1] * matrix.m[1][2] + m.m[3][2] * matrix.m[2][2]);
        matrix.m[3][3] = 1.0f;
        return matrix;
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

    public static Vec3d vectorIntersectPlane(Vec3d planeP, Vec3d planeN, Vec3d lineStart, Vec3d lineEnd) {
        planeN = vectorNormalize(planeN);
        float planeD = -vectorDotProduct(planeN, planeP);
        float ad = vectorDotProduct(lineStart, planeN);
        float bd = vectorDotProduct(lineEnd, planeN);
        float t = (-planeD - ad) / (bd - ad);
        Vec3d lineStartToEnd = vectorSubtract(lineEnd, lineStart);
        Vec3d lineToIntersect = vectorMultiply(lineStartToEnd, t);
        return vectorAdd(lineStart, lineToIntersect);
    }

    public static int polygonClipOnPlane(Vec3d planeP, Vec3d planeN, Polygon polyIn, Polygon[] polyOut) {
        planeN = vectorNormalize(planeN);

        // Return signed shortest distance from point to plane, plane normal must be normalised
        BiFunction<Vec3d, Vec3d, Float> distFunction = (p, pn) -> {
            Vec3d n = vectorNormalize(p); //todo проверить не нужно ли заменить переменную на p
            return (pn.x * p.x + pn.y * p.y + pn.z * p.z - vectorDotProduct(pn, planeP));
        };
        // Create
        // two temporary storage arrays to classify points either side of plane
        // If distance sign is positive, point lies on "inside" of plane
        Vec3d[] insidePoints = new Vec3d[3];  int nInsidePointCount = 0;
        Vec3d[] outsidePoints = new Vec3d[3]; int nOutsidePointCount = 0;

        // Get signed distance of each point in triangle to plane
        float d0 = distFunction.apply(polyIn.p[0], planeN);
        float d1 = distFunction.apply(polyIn.p[1], planeN);
        float d2 = distFunction.apply(polyIn.p[2], planeN);

        if (d0 >= 0) { insidePoints[nInsidePointCount++] = polyIn.p[0]; }
        else { outsidePoints[nOutsidePointCount++] = polyIn.p[0]; }
        if (d1 >= 0) { insidePoints[nInsidePointCount++] = polyIn.p[1]; }
        else { outsidePoints[nOutsidePointCount++] = polyIn.p[1]; }
        if (d2 >= 0) { insidePoints[nInsidePointCount++] = polyIn.p[2]; }
        else { outsidePoints[nOutsidePointCount++] = polyIn.p[2]; }

        // Now classify triangle points, and break the input triangle into
        // smaller output triangles if required. There are four possible
        // outcomes...

        if (nInsidePointCount == 0)
        {
            // All points lie on the outside of plane, so clip whole triangle
            // It ceases to exist

            return 0; // No returned triangles are valid
        }

        if (nInsidePointCount == 3)
        {
            // All points lie on the inside of plane, so do nothing
            // and allow the triangle to simply pass through
            polyOut[0] = polyIn;

            return 1; // Just the one returned original triangle is valid
        }

        if (nInsidePointCount == 1 && nOutsidePointCount == 2)
        {
            // Triangle should be clipped. As two points lie outside
            // the plane, the triangle simply becomes a smaller triangle

            // Copy appearance info to new triangle
            polyOut[0].color = polyIn.color;

            // The inside point is valid, so keep that...
            polyOut[0].p[0] = insidePoints[0];

            // but the two new points are at the locations where the
            // original sides of the triangle (lines) intersect with the plane
            polyOut[0].p[1] = vectorIntersectPlane(planeP, planeN, insidePoints[0], outsidePoints[0]);
            polyOut[0].p[2] = vectorIntersectPlane(planeP, planeN, insidePoints[0], outsidePoints[1]);

            return 1; // Return the newly formed single triangle
        }

        if (nInsidePointCount == 2 && nOutsidePointCount == 1)
        {
            // Triangle should be clipped. As two points lie inside the plane,
            // the clipped triangle becomes a "quad". Fortunately, we can
            // represent a quad with two new triangles

            // Copy appearance info to new triangles
            polyOut[0].color = polyIn.color;

            polyOut[1].color = polyIn.color;

            // The first triangle consists of the two inside points and a new
            // point determined by the location where one side of the triangle
            // intersects with the plane
            polyOut[0].p[0] = insidePoints[0];
            polyOut[0].p[1] = insidePoints[1];
            polyOut[0].p[2] = vectorIntersectPlane(planeP, planeN, insidePoints[0], outsidePoints[0]);

            // The second triangle is composed of one of he inside points, a
            // new point determined by the intersection of the other side of the
            // triangle and the plane, and the newly created point above
            polyOut[1].p[0] = insidePoints[1];
            polyOut[1].p[1] = polyOut[0].p[2];
            polyOut[1].p[2] = vectorIntersectPlane(planeP, planeN, insidePoints[1], outsidePoints[0]);

            return 2; // Return two newly formed triangles which form a quad
        }
        throw new RuntimeException("треугольника не нашлось как так");
    }
}
