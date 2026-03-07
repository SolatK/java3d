package core;

import core.graphics.Matrix4x4;
import core.graphics.Mesh;
import core.graphics.Polygon;
import core.graphics.Vec3d;
import core.utils.ModelLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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

    private Vec3d cameraPos = new Vec3d();
    private Vec3d lookDir;
    private float yaw;

    private final Canvas canvas = new Canvas(WIDTH, HEIGHT);

    private final Deque<MouseEvent> mouseDeque = new ArrayDeque<>();
    private final Deque<KeyEvent> keysDeque = new ArrayDeque<>();

    private final Matrix4x4 projectionMatrix = new Matrix4x4();

    public void run() {
        JFrame frame = frame(WIDTH, HEIGHT, canvas, "3D тест");
        frame.setResizable(false);

        canvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                keysDeque.addLast(e);
            }

            @Override
            public void keyTyped(KeyEvent e) {
                keysDeque.addLast(e);
            }
        });
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (mouseDeque.size() < 5) {
                    if (e.getButton() == 1) {
                        mouseDeque.addLast(e);
                    }
                    if (e.getButton() == 3) {
                        mouseDeque.addLast(e);
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mouseDeque.addLast(e);
            }
        });

        canvas.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (mouseDeque.size() > 5) return;
                mouseDeque.addLast(e);
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
            //TODO разобраться со временем
            now = System.nanoTime();
            elapsedTime = now - lastTime;
            lastTime = now;

            frameTime = elapsedTime / 1000000000f;

            processKeys(elapsedTime / 1000000000f);

            //physicsEngine.tick();

            render(canvas, elapsedTime / 1000000000f);

            waitTime = (long) ((TARGET_TIME_NANOS - (frameTime / 1000)) / 1000000);




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

    private void processKeys(float elapsedTime) {
        if (!keysDeque.isEmpty()) {
            KeyEvent e = keysDeque.pop();
            char keyChar = e.getKeyChar();

            Vec3d forward = vectorMultiply(lookDir, 8 * elapsedTime);
            if (keyChar == 'w') {
                cameraPos = vectorAdd(cameraPos, forward);
            } else if (keyChar == 's') {
                cameraPos = vectorSubtract(cameraPos, forward);
            } else if (keyChar == ' ') {
                cameraPos.y += 8 * elapsedTime;
            } else if (keyChar == 'c') {
                cameraPos.y -= 8 * elapsedTime;
            } else if (keyChar == 'a') {
                cameraPos.x += 8 * elapsedTime;
            } else if (keyChar == 'd') {
                cameraPos.x -= 8 * elapsedTime;
            }  else if (keyChar == 'q') {
                yaw -= 2 * elapsedTime;
            } else if (keyChar == 'e') {
                yaw += 2 * elapsedTime;
            }
        }

    }


    private void render(Canvas canvas, float frameTime) {
        canvas.newFrame();
        canvas.drawFPS(frameTime);

        Matrix4x4 matrixRotZ = new Matrix4x4();
        Matrix4x4 matrixRotX = new Matrix4x4();

        angle += 0.03f;
        matrixRotZ.makeRotationZ(0);
        matrixRotX.makeRotationX(0);

        Matrix4x4 translationMatrix = new Matrix4x4();
        translationMatrix.makeTranslation(0f, 0f, 16f);


        Matrix4x4 worldMatrix;

        worldMatrix = matrixMultiply(
                matrixMultiply(matrixRotZ, matrixRotX),
                translationMatrix
        );

        Vec3d vecUp = new Vec3d(0, 1, 0);
        Vec3d vecTarget = new Vec3d(0, 0, 1);
        Matrix4x4 cameraRotation = new Matrix4x4();
        cameraRotation.makeRotationY(yaw);
        lookDir = matrixMultiplyVector(cameraRotation, vecTarget);
        vecTarget = vectorAdd(cameraPos, lookDir);


        Matrix4x4 cameraMatrix = new Matrix4x4();
        cameraMatrix.makePointAt(cameraPos, vecTarget, vecUp);
        Matrix4x4 viewMatrix = matrixQuickInverse(cameraMatrix);


        Mesh model = ModelLoader.load(Paths.get("src/main/resources/axis.obj"));
        model.setColor(new Color(255, 140, 0));


        //полигоноукладка
        List<Polygon> polygonsToDraw = new ArrayList<>();

        for (Polygon polygon: model.mesh) {
            Polygon polygonProjected = new Polygon();
            Polygon polygonTransformed = new Polygon();
            Polygon polygonViewd = new Polygon();

            polygonTransformed.p[0] = matrixMultiplyVector(worldMatrix, polygon.p[0]);
            polygonTransformed.p[1] = matrixMultiplyVector(worldMatrix, polygon.p[1]);
            polygonTransformed.p[2] = matrixMultiplyVector(worldMatrix, polygon.p[2]);

            //нормаль
            Vec3d normal, lineA, lineB;

            lineA = vectorSubtract(polygonTransformed.p[1], polygonTransformed.p[0]);
            lineB = vectorSubtract(polygonTransformed.p[2], polygonTransformed.p[0]);

            //нормализация нормали
            normal = vectorNormalize(vectorCrossProduct(lineA, lineB));


            Vec3d cameraRay = vectorSubtract(polygonTransformed.p[0], cameraPos);

            //проверка на видимость полигона
            if (vectorDotProduct(normal, cameraRay) >= 0) continue;

            //свет и цвет
            Vec3d light = vectorNormalize(new Vec3d(0, 0, -1));
            float dp = vectorDotProduct(normal, light);

            polygonProjected.color = shade(polygon.color, dp);

            polygonViewd.p[0] = matrixMultiplyVector(viewMatrix, polygonTransformed.p[0]);
            polygonViewd.p[1] = matrixMultiplyVector(viewMatrix, polygonTransformed.p[1]);
            polygonViewd.p[2] = matrixMultiplyVector(viewMatrix, polygonTransformed.p[2]);


            //обрезка полигонов
            Polygon [] clipped = {new Polygon(), new Polygon()};
            int clippedPolygons = polygonClipOnPlane(
                    new Vec3d(0,0,0.1f),
                    new Vec3d(0,0,1f),
                    polygonViewd,
                    clipped
            );

            for (int i = 0; i < clippedPolygons; i++) {

                //проекция на координаты экрана
                polygonProjected.p[0] = matrixMultiplyVector(projectionMatrix, clipped[i].p[0]);
                polygonProjected.p[1] = matrixMultiplyVector(projectionMatrix, clipped[i].p[1]);
                polygonProjected.p[2] = matrixMultiplyVector(projectionMatrix, clipped[i].p[2]);


                polygonProjected.p[0] = vectorDivide(polygonProjected.p[0], polygonProjected.p[0].w);
                polygonProjected.p[1] = vectorDivide(polygonProjected.p[1], polygonProjected.p[1].w);
                polygonProjected.p[2] = vectorDivide(polygonProjected.p[2], polygonProjected.p[2].w);


                //x и y перевернуты
                polygonProjected.p[0].x *= -1.0f;
                polygonProjected.p[1].x *= -1.0f;
                polygonProjected.p[2].x *= -1.0f;
                polygonProjected.p[0].y *= -1.0f;
                polygonProjected.p[1].y *= -1.0f;
                polygonProjected.p[2].y *= -1.0f;


                //scale and offset into view
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

                polygonsToDraw.add(new Polygon(polygonProjected));
            }
        }

        polygonsToDraw.sort((s1, s2) -> {
            float a = (s1.p[0].z + s1.p[1].z + s1.p[2].z) / 3;
            float b = (s2.p[0].z + s2.p[1].z + s2.p[2].z) / 3;
            return Float.compare(b, a);
        });

        for (Polygon polygon: polygonsToDraw) {

            // Clip triangles against all four screen edges, this could yield
            // a bunch of triangles, so create a queue that we traverse to
            //  ensure we only test new triangles generated against planes

            Polygon[] clipped;
            Deque<Polygon> polygonDeque = new ArrayDeque<>();

            // Add initial triangle
            polygonDeque.addLast(polygon);
            int nNewTriangles = 1;

            for (int p = 0; p < 4; p++)
            {

                int nTrisToAdd = 0;
                while (nNewTriangles > 0)
                {
                    clipped = new Polygon[]{new Polygon(), new Polygon()};
                    // Take triangle from front of queue
                    Polygon test = polygonDeque.pop();
                    nNewTriangles--;

                    // Clip it against a plane. We only need to test each
                    // subsequent plane, against subsequent new triangles
                    // as all triangles after a plane clip are guaranteed
                    // to lie on the inside of the plane. I like how this
                    // comment is almost completely and utterly justified
                    switch (p)
                    {
                        case 0:	nTrisToAdd = polygonClipOnPlane(new Vec3d(0, 0, 0), new Vec3d(0, 1, 0), test, clipped); break;
                        case 1:	nTrisToAdd = polygonClipOnPlane(new Vec3d(0, HEIGHT - 1, 0), new Vec3d(0, -1, 0), test, clipped); break;
                        case 2:	nTrisToAdd = polygonClipOnPlane(new Vec3d(0, 0, 0),new Vec3d(1, 0, 0), test, clipped); break;
                        case 3:	nTrisToAdd = polygonClipOnPlane(new Vec3d(WIDTH - 1, 0, 0), new Vec3d(-1, 0, 0), test, clipped); break;
                    }

                    // Clipping may yield a variable number of triangles, so
                    // add these new ones to the back of the queue for subsequent
                    // clipping against next planes
                    for (int w = 0; w < nTrisToAdd; w++)
                        polygonDeque.addLast(clipped[w]);
                }
                nNewTriangles = polygonDeque.size();
            }


            // Draw the transformed, viewed, clipped, projected, sorted, clipped triangles
            for (Polygon p: polygonDeque)
            {
                canvas.fillTriangle(p);
                canvas.drawTriangle(p);
            }

        }

        canvas.render();
    }

    private Color shade(Color color, float factor) {
        factor = Math.max(0.1f, factor);
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
