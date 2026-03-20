package core;

import entity.Camera;
import entity.Chunk;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static core.Config.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Controller {

    private long window;


    //Камера
    private Camera camera = new Camera(new Vector3f(0, 0, 0));

    private Render render;

    //мышь
    private boolean firstMouse = true;
    private double lastX = 0, lastY = 0;

    private void init() {
        Config.load();

        //инициализируем матрицу проекции
        //Её нужно будет перегенерить если поменяется размер окна
        float aspect = (float) width / height;
        float fovVertical = (float) (2 * Math.atan(Math.tan(Math.toRadians(hFov) / 2) / aspect));

        Matrix4f projection = new Matrix4f().perspective(fovVertical, aspect, 0.1f, 1000f);



        //инициализация окошка
        if (!glfwInit()) throw new IllegalStateException("Не удалось инициализировать GLFW");

        window = glfwCreateWindow(width, height, title, NULL, NULL);
        if (window == NULL) throw new RuntimeException("Не удалось создать окно");


        glfwMakeContextCurrent(window);

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
    }

    Map<Vector3i, Chunk> chunks;
    List<Object> gameObjects;


    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            //клавомыш и может потом прочее
            handleInput();

            render.render(camera, gameObjects, chunks);

            glfwSwapBuffers(window);
            glfwPollEvents();

        }
    }


    private boolean pressed = false;

    private void handleInput() {
        float speed = 0.05f;
        Vector3f forward = camera.getFront();
        Vector3f up = camera.getUp();
        Vector3f pos = camera.getPos();

        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            pos.add(new Vector3f(forward).mul(speed));
        }
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            pos.add(new Vector3f(forward).mul(-speed));
        }
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            pos.add(new Vector3f(forward).cross(up).normalize().mul(speed));
        }
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            pos.add(new Vector3f(forward).cross(up).normalize().mul(-speed));
        }
        if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS) {
            pos.add(new Vector3f(up).mul(speed));
        }
        if (glfwGetKey(window, GLFW_KEY_C) == GLFW_PRESS) {
            pos.add(new Vector3f(up).mul(-speed));
        }

        //возвращаем курсор и снова скрываем
        if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS){
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        }
        if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS && !pressed) {
            //shoot(Materials.SAND);
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            pressed = true;
        }
        if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS && !pressed) {
            //shoot(Materials.AIR);
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            pressed = true;
        }
        if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_RELEASE && glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_RELEASE) {
            pressed = false;
        }

    }
}
