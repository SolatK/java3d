package utils;

import org.joml.Vector3i;

public class CubeData {
    // Координаты 8 углов куба
    public static final Vector3i[] OFFSETS = {
            new Vector3i(0, 0, 0), new Vector3i(1, 0, 0), new Vector3i(1, 0, 1), new Vector3i(0, 0, 1),
            new Vector3i(0, 1, 0), new Vector3i(1, 1, 0), new Vector3i(1, 1, 1), new Vector3i(0, 1, 1)
    };

    // Какое ребро (0-11) какие углы (0-7) соединяет
    public static final int[][] EDGE_CONNECTIONS = {
            {0, 1}, {1, 2}, {2, 3}, {3, 0}, // нижние
            {4, 5}, {5, 6}, {6, 7}, {7, 4}, // верхние
            {0, 4}, {1, 5}, {2, 6}, {3, 7}  // вертикальные
    };
}