package core;

import core.Canvas;
import core.graphics.Matrix;
import core.graphics.Mesh;
import core.graphics.Polygon;
import core.graphics.Vec3d;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

import static core.Settings.*;

public class Controller {

    private float frameTime;

    private double angle;

    private final Canvas canvas = new Canvas(WIDTH, HEIGHT);

    private final Deque<MouseEvent> pressedDeque = new ArrayDeque<>();

    private Mesh cube;

    private Matrix projectionMatrix = new Matrix();

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

        System.out.println(cube);


        //noinspection InfiniteLoopStatement
        while (true) {
            now = System.nanoTime();
            elapsedTime = now - lastTime;
            lastTime = now;

            frameTime = (elapsedTime / 1000000000);

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

        angle += 0.01f;
        System.out.println(angle);

        // Rotation Z
        matrixRotZ.m[0][0] = (float) Math.cos(angle);
        matrixRotZ.m[0][1] = (float) Math.sin(angle);
        matrixRotZ.m[1][0] = (float) -Math.sin(angle);
        matrixRotZ.m[1][1] = (float) Math.cos(angle);
        matrixRotZ.m[2][2] = 1;
        matrixRotZ.m[3][3] = 1;

        // Rotation X
        matrixRotX.m[0][0] = 1;
        matrixRotX.m[1][1] = (float) Math.cos(angle * 0.5f);
        matrixRotX.m[1][2] = (float) Math.sin(angle * 0.5f);
        matrixRotX.m[2][1] = (float) -Math.sin(angle * 0.5f);
        matrixRotX.m[2][2] = (float) Math.cos(angle * 0.5f);
        matrixRotX.m[3][3] = 1;


        //полигоноукладка
        for (Polygon polygon: cube.mesh) {
            Polygon polygonProjected = new Polygon();
            Polygon polygonRotatedZ = new Polygon();
            Polygon polygonRotatedZX = new Polygon();


            multiplyMatrixVector(polygon.p[0], polygonRotatedZ.p[0], matrixRotZ);
            multiplyMatrixVector(polygon.p[1], polygonRotatedZ.p[1], matrixRotZ);
            multiplyMatrixVector(polygon.p[2], polygonRotatedZ.p[2], matrixRotZ);

            multiplyMatrixVector(polygonRotatedZ.p[0], polygonRotatedZX.p[0], matrixRotX);
            multiplyMatrixVector(polygonRotatedZ.p[1], polygonRotatedZX.p[1], matrixRotX);
            multiplyMatrixVector(polygonRotatedZ.p[2], polygonRotatedZX.p[2], matrixRotX);


            Polygon polygonTranslated = new Polygon(polygonRotatedZX);

            polygonTranslated.p[0].z = polygonRotatedZX.p[0].z + 3f;
            polygonTranslated.p[1].z = polygonRotatedZX.p[1].z + 3f;
            polygonTranslated.p[2].z = polygonRotatedZX.p[2].z + 3f;


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

            canvas.drawTriangle(
                    (int) polygonProjected.p[0].x, (int) polygonProjected.p[0].y,
                    (int) polygonProjected.p[1].x, (int) polygonProjected.p[1].y,
                    (int) polygonProjected.p[2].x, (int) polygonProjected.p[2].y,
                    Color.green
            );
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
