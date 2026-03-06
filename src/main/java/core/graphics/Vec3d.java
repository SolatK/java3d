package core.graphics;

public class Vec3d {
    public float x;
    public float y;
    public float z;

    public Vec3d() {
        x = 0;
        y = 0;
        z = 0;
    }
    public Vec3d(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3d(float[] coords) {
        this.x = coords[0];
        this.y = coords[1];
        this.z = coords[2];
    }
}
