package core;

import core.graphics.Matrix;
import core.graphics.Mesh;
import core.graphics.Polygon;
import core.graphics.Vec3d;
import core.utils.ModelLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static core.Settings.*;

public class Controller {

    private float frameTime;

    //TODO временный мусор
    private double angle;

    //TODO временный мусор
    private Vec3d camera = new Vec3d();

    private final Canvas canvas = new Canvas(WIDTH, HEIGHT);

    private final Deque<MouseEvent> pressedDeque = new ArrayDeque<>();

    //TODO временный мусор
    //private Mesh cube;

    private final Matrix projectionMatrix = new Matrix();

    public void run() {
        JFrame frame = frame(WIDTH, HEIGHT, canvas, "3D тест");
        frame.setResizable(false);
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (pressedDeque.size() < 5) {
                    if (e.getButton() == 1) {
                        pressedDeque.addLast(e);
                    }
                    if (e.getButton() == 3) {
                        pressedDeque.addLast(e);
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressedDeque.addLast(e);
            }
        });

        canvas.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (pressedDeque.size() > 5) return;
                pressedDeque.addLast(e);
            }
        });


        long lastTime = System.nanoTime();
        long now;
        long elapsedTime;
        long waitTime;

        /*
        cube = new Mesh(
            // SOUTH
            new float[]{ 0.0f, 0.0f, 0.0f,    0.0f, 1.0f, 0.0f,    1.0f, 1.0f, 0.0f },
            new float[]{ 0.0f, 0.0f, 0.0f,    1.0f, 1.0f, 0.0f,    1.0f, 0.0f, 0.0f },

            // EAST
            new float[]{ 1.0f, 0.0f, 0.0f,    1.0f, 1.0f, 0.0f,    1.0f, 1.0f, 1.0f },
            new float[]{ 1.0f, 0.0f, 0.0f,    1.0f, 1.0f, 1.0f,    1.0f, 0.0f, 1.0f },

            // NORTH
            new float[]{ 1.0f, 0.0f, 1.0f,    1.0f, 1.0f, 1.0f,    0.0f, 1.0f, 1.0f },
            new float[]{ 1.0f, 0.0f, 1.0f,    0.0f, 1.0f, 1.0f,    0.0f, 0.0f, 1.0f },

            // WEST
            new float[]{ 0.0f, 0.0f, 1.0f,    0.0f, 1.0f, 1.0f,    0.0f, 1.0f, 0.0f },
            new float[]{ 0.0f, 0.0f, 1.0f,    0.0f, 1.0f, 0.0f,    0.0f, 0.0f, 0.0f },

            // TOP
            new float[]{ 0.0f, 1.0f, 0.0f,    0.0f, 1.0f, 1.0f,    1.0f, 1.0f, 1.0f },
            new float[]{ 0.0f, 1.0f, 0.0f,    1.0f, 1.0f, 1.0f,    1.0f, 1.0f, 0.0f },

            // BOTTOM
            new float[]{ 1.0f, 0.0f, 1.0f,    0.0f, 0.0f, 1.0f,    0.0f, 0.0f, 0.0f },
            new float[]{ 1.0f, 0.0f, 1.0f,    0.0f, 0.0f, 0.0f,    1.0f, 0.0f, 0.0f }
        );

        cube.setColor(Color.orange);
         */


        //projection matrix
        float fNear = 0.1f;
        float fFar = 1000f;
        float fov = 90f;
        float aspectRatio = (float) HEIGHT / (float) WIDTH;
        float fovRad = (float) (1f / Math.tan(fov * 0.5 / 180f * Math.PI));

        projectionMatrix.m[0][0] = aspectRatio * fovRad;
        projectionMatrix.m[1][1] = fovRad;
        projectionMatrix.m[2][2] = fFar / (fFar - fNear);
        projectionMatrix.m[3][2] = (-fFar * fNear) / (fFar - fNear);
        projectionMatrix.m[2][3] = 1f;
        projectionMatrix.m[3][3] = 0f;


        //noinspection InfiniteLoopStatement
        while (true) {
            now = System.nanoTime();
            elapsedTime = now - lastTime;
            lastTime = now;

            frameTime = ((float) elapsedTime / 1000000000);

            processKeys();

            //physicsEngine.tick();

            render(canvas, (elapsedTime / 1000000));

            long timeTaken = System.nanoTime() - now;
            waitTime = (TARGET_TIME_NANOS - timeTaken) / 1000000;




            if (waitTime > 0) {
                try {
                    //noinspection BusyWait
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    //noinspection CallToPrintStackTrace
                    e.printStackTrace();
                }
            } else {
                //сделать фреймскип если не хватает фпс
            }
        }
    }

    private int pressedFlag = 0;
    private MouseEvent lastEvent;

    private void processKeys() {

    }


    private void render(Canvas canvas, long frameTime) {
        canvas.newFrame();
        canvas.drawFPS(frameTime);
        Matrix matrixRotZ = new Matrix();
        Matrix matrixRotX = new Matrix();

        angle += 0.03f;

        // Rotation Z матрица
        matrixRotZ.m[0][0] = (float) Math.cos(angle);
        matrixRotZ.m[0][1] = (float) Math.sin(angle);
        matrixRotZ.m[1][0] = (float) -Math.sin(angle);
        matrixRotZ.m[1][1] = (float) Math.cos(angle);
        matrixRotZ.m[2][2] = 1;
        matrixRotZ.m[3][3] = 1;

        // Rotation X матрица
        matrixRotX.m[0][0] = 1;
        matrixRotX.m[1][1] = (float) Math.cos(angle * 0.5f);
        matrixRotX.m[1][2] = (float) Math.sin(angle * 0.5f);
        matrixRotX.m[2][1] = (float) -Math.sin(angle * 0.5f);
        matrixRotX.m[2][2] = (float) Math.cos(angle * 0.5f);
        matrixRotX.m[3][3] = 1;


        Mesh model = ModelLoader.load(Paths.get("src/main/resources/SpaceShip.obj"));
        //полигоноукладка
        List<Polygon> polygonsToDraw = new ArrayList<>();

        for (Polygon polygon: model.mesh) {
            Polygon polygonProjected = new Polygon(polygon.color);
            Polygon polygonRotatedZ = new Polygon();
            Polygon polygonRotatedZX = new Polygon();


            multiplyMatrixVector(polygon.p[0], polygonRotatedZ.p[0], matrixRotZ);
            multiplyMatrixVector(polygon.p[1], polygonRotatedZ.p[1], matrixRotZ);
            multiplyMatrixVector(polygon.p[2], polygonRotatedZ.p[2], matrixRotZ);

            multiplyMatrixVector(polygonRotatedZ.p[0], polygonRotatedZX.p[0], matrixRotX);
            multiplyMatrixVector(polygonRotatedZ.p[1], polygonRotatedZX.p[1], matrixRotX);
            multiplyMatrixVector(polygonRotatedZ.p[2], polygonRotatedZX.p[2], matrixRotX);


            Polygon polygonTranslated = new Polygon(polygonRotatedZX);

            polygonTranslated.p[0].z = polygonRotatedZX.p[0].z + 8f;
            polygonTranslated.p[1].z = polygonRotatedZX.p[1].z + 8f;
            polygonTranslated.p[2].z = polygonRotatedZX.p[2].z + 8f;

            //нормаль
            Vec3d normal = new Vec3d();
            Vec3d lineA = new Vec3d();
            Vec3d lineB = new Vec3d();

            lineA.x = polygonTranslated.p[1].x - polygonTranslated.p[0].x;
            lineA.y = polygonTranslated.p[1].y - polygonTranslated.p[0].y;
            lineA.z = polygonTranslated.p[1].z - polygonTranslated.p[0].z;

            lineB.x = polygonTranslated.p[2].x - polygonTranslated.p[0].x;
            lineB.y = polygonTranslated.p[2].y - polygonTranslated.p[0].y;
            lineB.z = polygonTranslated.p[2].z - polygonTranslated.p[0].z;

            normal.x = lineA.y * lineB.z - lineA.z * lineB.y;
            normal.y = lineA.z * lineB.x - lineA.x * lineB.z;
            normal.z = lineA.x * lineB.y - lineA.y * lineB.x;

            //нормализация нормали
            float l = (float) Math.sqrt(normal.x*normal.x + normal.y*normal.y + normal.z*normal.z);
            normal.x /= l; normal.y /= l; normal.z /= l;

            if (
                normal.x * (polygonTranslated.p[0].x - camera.x) +
                normal.y * (polygonTranslated.p[0].y - camera.y) +
                normal.z * (polygonTranslated.p[0].z - camera.z) >= 0
            ) continue;

            //свет
            //TODO нормализовать свет?
            Vec3d light = new Vec3d(0, 0, -1);
            float dp = dotProduct(normal, light);

            polygonProjected.color = shade(polygonProjected.color, dp);

            //проекция на координаты экрана
            multiplyMatrixVector(polygonTranslated.p[0], polygonProjected.p[0], projectionMatrix);
            multiplyMatrixVector(polygonTranslated.p[1], polygonProjected.p[1], projectionMatrix);
            multiplyMatrixVector(polygonTranslated.p[2], polygonProjected.p[2], projectionMatrix);

            //scale into view
            polygonProjected.p[0].x += 1;
            polygonProjected.p[0].y += 1;

            polygonProjected.p[1].x += 1;
            polygonProjected.p[1].y += 1;

            polygonProjected.p[2].x += 1;
            polygonProjected.p[2].y += 1;


            polygonProjected.p[0].x *= 0.5f * WIDTH;
            polygonProjected.p[0].y *= 0.5f * HEIGHT;

            polygonProjected.p[1].x *= 0.5f * WIDTH;
            polygonProjected.p[1].y *= 0.5f * HEIGHT;

            polygonProjected.p[2].x *= 0.5f * WIDTH;
            polygonProjected.p[2].y *= 0.5f * HEIGHT;

            /*canvas.fillTriangle(
                    (int) polygonProjected.p[0].x, (int) polygonProjected.p[0].y,
                    (int) polygonProjected.p[1].x, (int) polygonProjected.p[1].y,
                    (int) polygonProjected.p[2].x, (int) polygonProjected.p[2].y,
                    new Color((int) Math.max(0, dp * 255), (int) Math.max(0, dp * 255), 0)
            );
             */

            polygonsToDraw.add(polygonProjected);
        }

        polygonsToDraw.sort((s1, s2) -> {
            float a = (s1.p[0].z + s1.p[1].z + s1.p[2].z) / 3;
            float b = (s2.p[0].z + s2.p[1].z + s2.p[2].z) / 3;
            return Float.compare(b, a);
        });

        for (Polygon polygon: polygonsToDraw) {
            canvas.fillTriangle(polygon);
        }

        canvas.render();
    }

    private void multiplyMatrixVector(Vec3d i, Vec3d o, Matrix m) {
        o.x = i.x * m.m[0][0] + i.y * m.m[1][0] + i.z * m.m[2][0] + m.m[3][0];
        o.y = i.x * m.m[0][1] + i.y * m.m[1][1] + i.z * m.m[2][1] + m.m[3][1];
        o.z = i.x * m.m[0][2] + i.y * m.m[1][2] + i.z * m.m[2][2] + m.m[3][2];
        float w = i.x * m.m[0][3] + i.y * m.m[1][3] + i.z * m.m[2][3] + m.m[3][3];

        if (w != 0)
        {
            o.x /= w; o.y /= w; o.z /= w;
        }
    }

    private float dotProduct(Vec3d vecA, Vec3d vecB) {
        return vecA.x * vecB.x + vecA.y * vecB.y + vecA.z * vecB.z;
    }

    private Color shade(Color color, float factor) {
        factor = Math.max(0, factor);
        return new Color(
                (int) (color.getRed() * factor),
                (int) (color.getGreen() * factor),
                (int) (color.getBlue() * factor)
        );
    }


    public JFrame frame(int width, int height, Canvas canvas, String title) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(width, height);
        frame.add(canvas);
        frame.pack();
        frame.setVisible(true);

        canvas.createBufferStrategy(2);
        canvas.setBs(canvas.getBufferStrategy());
        return frame;
    }
}
