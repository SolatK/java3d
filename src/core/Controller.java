package core;

import entity.Camera;
import entity.Chunk;
import graphics.Mesh;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL;
import utils.ChunkStatus;
import utils.TerrainMeshGenerator;
import utils.RaycastResult;

import java.io.IOException;
import java.util.List;

import static core.Config.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static core.Config.chunkPerFrame;
import static core.Config.targetFps;

public class Controller {

    private long window;

    //Камера
    private Camera camera = new Camera(new Vector3f(0, 0, 0));

    private Render render;

    private World world;

    //мышь
    private boolean firstMouse = true;
    private double lastX = 0, lastY = 0;


    //стабилизация фпс
    private long nsPerFrame; // Время одного кадра в наносекундах
    private long lastTime = System.nanoTime();
    private float nsDeltaTime; //дельта в наносекундах

    //вывод фпс
    private double lastFpsTime = 0; // Время последнего обновления счетчика
    private int fpsCount = 0;       // Счетчик кадров

    //скорость камеры
    float speed = 10f;

    private void init() {
        Config.load();

        nsPerFrame = (1000000000 / (long) targetFps); // Время одного кадра в секундах

        //инициализируем матрицу проекции
        //Её нужно будет перегенерить если поменяется размер окна
        float aspect = (float) width / height;
        float fovVertical = (float) (2 * Math.atan(Math.tan(Math.toRadians(hFov) / 2) / aspect));

        Matrix4f projection = new Matrix4f().perspective(fovVertical, aspect, 0.1f, 5000f);



        //инициализация окошка
        if (!glfwInit()) throw new IllegalStateException("Не удалось инициализировать GLFW");

        window = glfwCreateWindow(width, height, title, NULL, NULL);
        if (window == NULL) throw new RuntimeException("Не удалось создать окно");


        glfwMakeContextCurrent(window);

        glfwSwapInterval(0);

        //мышь
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        glfwSetCursorPosCallback(window, (windowHandle, xpos, ypos) -> {
            if (firstMouse) {
                lastX = xpos;
                lastY = ypos;
                firstMouse = false;
            }

            // Вычисляем смещение
            float xoffset = (float) (xpos - lastX);
            float yoffset = (float) (lastY - ypos); // Y инвертирован

            lastX = xpos;
            lastY = ypos;

            float sensitivity = 0.1f;
            xoffset *= sensitivity;
            yoffset *= sensitivity;

            // Передаем данные в камеру
            camera.addYaw(xoffset);
            camera.addPitch(yoffset);
            camera.updateVectors();
        });


        GL.createCapabilities();

        world = new World();

        try {
            render = new Render();
        } catch (IOException e) {
            //не получилось прочитать код шейдера
            //TODO вынести компиляцию шейдеров
            throw new RuntimeException(e);
        }
        render.setProjection(projection);

        //дебаг штуки
        //glEnable(GL_DEBUG_OUTPUT);
        //glDebugMessageCallback((source, type, id, severity, length, message, userParam) -> {
        //    System.err.println("GL Callback: " + getDebugMessage(message, length));
        ///}, 0);

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS); // Отрисовывать только то, что ближе к камере
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
    }

    public void run() {
        init();

        loop();

        glfwDestroyWindow(window);
        glfwTerminate();
        world.destroy();
    }

    List<Object> gameObjects;


    private void loop() {
        while (!glfwWindowShouldClose(window)) {

            long now = System.nanoTime();
            long passedNs = now - lastTime; //в секундах

            if (passedNs >= nsPerFrame) {
                nsDeltaTime = (passedNs / 1000000000f);
                lastTime = now;

                //клавомыш и может потом прочее
                handleInput();

                updateGame();
                render.render(camera, gameObjects, world);


                glfwSwapBuffers(window);
                glfwPollEvents();

                double currentTime = glfwGetTime(); // Время в секундах с начала игры
                fpsCount++;

                if (currentTime - lastFpsTime >= 1.0) {
                    int displayFps = fpsCount;
                    fpsCount = 0;
                    lastFpsTime = currentTime;

                    // Выводим в заголовок окна
                    glfwSetWindowTitle(window, title + " | FPS: " + displayFps);
                }

            } else {
                try {
                    // Спим чуть меньше, чем нужно, чтобы не пропустить тайминг из-за неточности Thread.sleep
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private void updateGame() {
        //лимит генерации чанков за кадр
        int limit = chunkPerFrame;
        while (limit-- > 0 &&!world.getReadyToUpload().isEmpty()) {
            Chunk chunk = world.getReadyToUpload().poll();

            // ВАЖНО: Выполняем OpenGL команды здесь
            Mesh mesh = TerrainMeshGenerator.uploadMesh(chunk.getMeshData());
            chunk.setMesh(mesh);
            chunk.setStatus(ChunkStatus.READY);

            // Очищаем массив в оперативной памяти (он уже в видеокарте)
            chunk.setMeshData(null);
        }

        Vector3f playerPos = camera.getPos();
        int pCX = (int) Math.floor(playerPos.x / World.CHUNK_SIZE);
        int pCZ = (int) Math.floor(playerPos.z / World.CHUNK_SIZE);


        //todo сделать центральные чанки приоритетнее
        for (int x = -renderDistance; x <= renderDistance; x++) {
            for (int z = -renderDistance; z <= renderDistance; z++) {
                for (int cy = -depth; cy <= depth; cy++) {

                    int cx = pCX + x;
                    int cz = pCZ + z;

                    //запрос на генерацию в многопотоке
                    world.requestChunk(cx, cy, cz);
                }
            }
        }

        world.updateDirtyChunks();
        world.cleanupChunks(pCX, pCZ);

    }


    private boolean pressed = false;

    private void handleInput() {
        double safeDelta = (float) Math.min(nsDeltaTime, 0.1);
        float distance = (float) (speed * safeDelta);

        Vector3f forward = camera.getFront();
        Vector3f up = camera.getUp();
        Vector3f pos = camera.getPos();

        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            pos.add(new Vector3f(forward).mul(distance));
        }
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            pos.add(new Vector3f(forward).mul(-distance));
        }
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            pos.add(new Vector3f(forward).cross(up).normalize().mul(distance));
        }
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            pos.add(new Vector3f(forward).cross(up).normalize().mul(-distance));
        }
        if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS) {
            pos.add(new Vector3f(up).mul(distance));
        }
        if (glfwGetKey(window, GLFW_KEY_C) == GLFW_PRESS) {
            pos.add(new Vector3f(up).mul(-distance));
        }

        //возвращаем курсор и снова скрываем
        if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS){
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        }
        if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS && !pressed) {
            RaycastResult hit = world.raycast(camera.getPos(), camera.getFront(), 50.0f);
            if (hit != null) {
                // увеличиваем плотность (ставим)
                world.modifyTerrain(hit.worldPos(), 25f, 1);
            }
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            //pressed = true;
        }
        if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS) {
            RaycastResult hit = world.raycast(camera.getPos(), camera.getFront(), 50.0f);
            if (hit != null) {
                // Уменьшаем плотность (копаем)
                world.modifyTerrain(hit.worldPos(), 25f, -1);
            }
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        }
        if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_RELEASE && glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_RELEASE) {
            pressed = false;
        }

    }
}
