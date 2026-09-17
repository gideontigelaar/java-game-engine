package nl.team3;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryUtil;

import nl.team3.engine.core.Config;
import nl.team3.engine.core.SceneManager;
import nl.team3.games.tictactoe.TicTacToeScene;

public class App {
    public static void main(String[] args) {
        Configuration.GLFW_CHECK_THREAD0.set(false);

        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

        long window = GLFW.glfwCreateWindow(Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT, Config.WINDOW_TITLE, MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create GLFW window");
        }

        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(Config.VSYNC_ENABLED ? 1 : 0);
        GLFW.glfwShowWindow(window);

        GL.createCapabilities();

        SceneManager sceneManager = new SceneManager();
        sceneManager.changeScene(new TicTacToeScene());

        // Dummy deltaTime
        float deltaTime = 0.016f;

        while (!GLFW.glfwWindowShouldClose(window)) {
            GL11.glClearColor(0.15f, 0.15f, 0.18f, 1.0f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            sceneManager.update(deltaTime);
            sceneManager.render();

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }

        sceneManager.cleanup();
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }
}