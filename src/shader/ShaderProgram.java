package shader;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11C.GL_FALSE;
import static org.lwjgl.opengl.GL20C.*;

public class ShaderProgram {
    private final int programId;
    private final Map<String, Integer> uniforms = new HashMap<>();

    public ShaderProgram(String vertexCode, String fragmentCode) {
        programId = glCreateProgram();

        int vShader = compileShader(vertexCode, GL_VERTEX_SHADER);
        int fShader = compileShader(fragmentCode, GL_FRAGMENT_SHADER);

        glAttachShader(programId, vShader);
        glAttachShader(programId, fShader);
        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Ошибка линковки: " + glGetProgramInfoLog(programId));
        }

        // После линковки сами шейдеры можно удалить, программа их уже содержит
        glDetachShader(programId, vShader);
        glDetachShader(programId, fShader);
        glDeleteShader(vShader);
        glDeleteShader(fShader);
    }

    private int compileShader(String code, int type) {
        int shaderId = glCreateShader(type);
        glShaderSource(shaderId, code);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Ошибка компиляции шейдера: " + glGetShaderInfoLog(shaderId));
        }
        return shaderId;
    }

    public void bind() { glUseProgram(programId); }
    public void unbind() { glUseProgram(0); }

    // Поиск ID переменной в шейдере (кэшируем для скорости)
    private int getUniformLocation(String name) {
        return uniforms.computeIfAbsent(name, k -> glGetUniformLocation(programId, k));
    }


    public void setUniform(String name, Matrix4f matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            matrix.get(buffer);
            glUniformMatrix4fv(getUniformLocation(name), false, buffer);
        }
    }

    //позиция камеры
    public void setUniform(String name, Vector3f pos) {
        int location = glGetUniformLocation(programId, name);

        if (location != -1) {
            glUniform3f(location, pos.x, pos.y, pos.z);
        } else {
            System.err.println("Uniform '" + name + "' не найден в шейдере!");
        }
    }

    //Текстуры
    public void setUniform(String name, int value) {

        int location = glGetUniformLocation(programId, name);

        if (location != -1) {

            glUniform1i(location, value);
        } else {
            System.err.println("Uniform '" + name + "' не найден в шейдере!");
        }
    }

    //палитра материалов
    public void setUniform(String name, float[] pallete) {
        int location = glGetUniformLocation(programId, name);

        if (location != -1) {
            glUniform3fv(location, pallete);
        } else {
            System.err.println("Uniform '" + name + "' не найден в шейдере!");
        }
    }

    public void cleanup() {
        unbind();
        if (programId != 0) glDeleteProgram(programId);
    }
}
