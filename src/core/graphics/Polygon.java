package core.graphics;

public class Polygon {
    public Vec3d[] p = {new Vec3d(0,0,0), new Vec3d(0,0,0), new Vec3d(0,0,0)};

    public Polygon() {
    }

    public Polygon(Vec3d[] polygon) {
        this.p = polygon;
    }

    public Polygon(
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3
    ) {
        p = new Vec3d[]{
                new Vec3d(x1, y1, z1),
                new Vec3d(x2, y2, z2),
                new Vec3d(x3, y3, z3)
        };
    }

    public Polygon(float[] coords) {
        p = new Vec3d[]{
                new Vec3d(coords[0], coords[1], coords[2]),
                new Vec3d(coords[3], coords[4], coords[5]),
                new Vec3d(coords[6], coords[7], coords[8])
        };
    }

    public Polygon(Polygon polygon) {
        p = new Vec3d[]{
                new Vec3d(polygon.p[0].x, polygon.p[0].y, polygon.p[0].z),
                new Vec3d(polygon.p[1].x, polygon.p[1].y, polygon.p[1].z),
                new Vec3d(polygon.p[2].x, polygon.p[2].y, polygon.p[2].z)
        };
    }
}
