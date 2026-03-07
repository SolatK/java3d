package core;

import core.graphics.Matrix4x4;
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
import static core.utils.VectorMath.*;

public class Controller {

    private float frameTime;

    //TODO временный мусор
    private float angle;

    //TODO временный мусор
    private Vec3d camera = new Vec3d();

    private final Canvas canvas = new Canvas(WIDTH, HEIGHT);

    private final Deque<MouseEvent> pressedDeque = new ArrayDeque<>();

    private final Matrix4x4 projectionMatrix = new Matrix4x4();

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

        //projection matrix
        projectionMatrix.makeProjection(90f, (float) HEIGHT / (float) WIDTH, 0.1f, 1000f);

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

        Matrix4x4 matrixRotZ = new Matrix4x4();
        Matrix4x4 matrixRotX = new Matrix4x4();

        angle += 0.03f;
        matrixRotZ.makeRotationZ(angle);
        matrixRotX.makeRotationX(angle);

        Matrix4x4 translationMatrix = new Matrix4x4();
        translationMatrix.makeTranslation(0f, 0f, 16f);


        Matrix4x4 worldMatrix;

        worldMatrix = matrixMultiply(
                matrixMultiply(matrixRotZ, matrixRotX),
                translationMatrix
        );

        Mesh model = ModelLoader.load(Paths.get("src/main/resources/SpaceShip.obj"));
        model.setColor(new Color(255, 140, 0));


        //полигоноукладка
        List<Polygon> polygonsToDraw = new ArrayList<>();

        for (Polygon polygon: model.mesh) {
            Polygon polygonProjected = new Polygon();
            Polygon polygonTransformed = new Polygon();

            polygonTransformed.p[0] = matrixMultiplyVector(worldMatrix, polygon.p[0]);
            polygonTransformed.p[1] = matrixMultiplyVector(worldMatrix, polygon.p[1]);
            polygonTransformed.p[2] = matrixMultiplyVector(worldMatrix, polygon.p[2]);

            //нормаль
            Vec3d normal, lineA, lineB;

            lineA = vectorSubtract(polygonTransformed.p[1], polygonTransformed.p[0]);
            lineB = vectorSubtract(polygonTransformed.p[2], polygonTransformed.p[0]);

            //нормализация нормали
            normal = vectorNormalize(vectorCrossProduct(lineA, lineB));

            //TODO изучить вопрос
            Vec3d cameraRay = vectorSubtract(polygonTransformed.p[0], camera);

            //проверка на видимость полигона
            if (vectorDotProduct(normal, cameraRay) < 0) continue;

            //свет и цвет
            Vec3d light = vectorNormalize(new Vec3d(0, 1, -1));
            float dp = vectorDotProduct(normal, light);

            polygonTransformed.color = shade(polygon.color, dp);

            //проекция на координаты экрана
            polygonProjected.p[0] = matrixMultiplyVector(projectionMatrix, polygonTransformed.p[0]);
            polygonProjected.p[1] = matrixMultiplyVector(projectionMatrix, polygonTransformed.p[1]);
            polygonProjected.p[2] = matrixMultiplyVector(projectionMatrix, polygonTransformed.p[2]);


            polygonProjected.p[0] = vectorDivide(polygonProjected.p[0], polygonTransformed.p[0].w);
            polygonProjected.p[1] = vectorDivide(polygonProjected.p[1], polygonTransformed.p[1].w);
            polygonProjected.p[2] = vectorDivide(polygonProjected.p[2], polygonTransformed.p[2].w);

            //scale and offset into view
            //TODO убрать и посмотреть результат
            Vec3d offsetVec = new Vec3d(1, 1, 0);
            polygonProjected.p[0] = vectorAdd(polygonProjected.p[0], offsetVec);
            polygonProjected.p[1] = vectorAdd(polygonProjected.p[1], offsetVec);
            polygonProjected.p[2] = vectorAdd(polygonProjected.p[2], offsetVec);


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
