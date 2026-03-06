package core.graphics;

import java.util.Arrays;

public class Matrix {

    public float[][] m;
    {
        m = new float[4][4];
        Arrays.stream(m).forEach(row -> Arrays.fill(row, 0));
    }
}
