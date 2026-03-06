package core.graphics;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Mesh {
    public List<Polygon> mesh = new ArrayList<>();

    public Mesh(List<Polygon> mesh) {
        this.mesh = mesh;
    }

    public Mesh() {
    }


    public Mesh(float... coords) {
        //TODO добавить исключение на неверное количество координат
        for (int i = 0; i < coords.length; i+=9) {
            mesh.add( new Polygon(
                    coords[  i  ], coords[i + 1], coords[i + 2],
                    coords[i + 3], coords[i + 4], coords[i + 5],
                    coords[i + 6], coords[i + 7], coords[i + 8]
            ));
        }
    }

    public Mesh(float[]... coords) {
        //TODO добавить исключение на неверное количество координат

        for (float[] coord : coords) {
            mesh.add(
                    new Polygon(coord)
            );
        }
    }

    public void setColor(Color color) {
        for (Polygon polygon: mesh) {
            polygon.color = color;
        }
    }

    public void addPolygon(Polygon polygon) {
        mesh.add(polygon);
    }
}
