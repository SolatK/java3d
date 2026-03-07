package core.graphics;

public class Vec3d {
    public float x = 0;
    public float y = 0;
    public float z = 0;
    public float w = 1;

    public Vec3d() {

    }

    public Vec3d(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vec3d(float x, float y, float z) {
        this(x, y, z, 1);
    }

    public Vec3d(float[] coords) {
        this.x = coords[0];
        this.y = coords[1];
        this.z = coords[2];
    }
}
