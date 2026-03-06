package core.utils;

import core.graphics.Mesh;
import core.graphics.Polygon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ModelLoader {
    public static Mesh load(Path path) {
        Mesh mesh = new Mesh();

        List<Float[]> vertexes;
        List<Integer[]> faces ;

        //TODO переделать с использованием scanner
        try (Stream<String> file = Files.lines(path)) {

            vertexes = file.filter(line -> line.charAt(0) == 'v')
                    .map(s -> Arrays.stream(s.substring(2).split(" "))
                    .map(Float::parseFloat)
                    .toArray(Float[]::new))
                    .toList();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (Stream<String> file = Files.lines(path)) {

            //TODO убрать костыль с двойным чтением файла
            faces = file.filter(line -> line.charAt(0) == 'f')
                    .map(s -> Arrays.stream(s.substring(2).split(" "))
                    .map(i -> Integer.parseInt(i) - 1)// -1 для соответствия индексов
                    .toArray(Integer[]::new))
                    .toList();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (Integer[] face: faces) {
            mesh.addPolygon(
                new Polygon(
                    vertexes.get(face[0])[0], vertexes.get(face[0])[1], vertexes.get(face[0])[2],
                    vertexes.get(face[1])[0], vertexes.get(face[1])[1], vertexes.get(face[1])[2],
                    vertexes.get(face[2])[0], vertexes.get(face[2])[1], vertexes.get(face[2])[2]
                )
            );
        }

        return mesh;
    }
}
