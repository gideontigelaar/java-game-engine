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

        double lastTime = GLFW.glfwGetTime();

        // FPS tracking
        float fpsTimer = 0.0f;
        int frames = 0;

        while (!GLFW.glfwWindowShouldClose(window)) {
            // Calc delta time
            double currentTime = GLFW.glfwGetTime();
            float deltaTime = (float) (currentTime - lastTime);
            lastTime = currentTime;

            // Track frames and time
            frames++;
            fpsTimer += deltaTime;

            // Update window title every second
            if (fpsTimer >= 1.0f) {
                GLFW.glfwSetWindowTitle(window, Config.WINDOW_TITLE + " | FPS: " + frames);
                frames = 0;
                fpsTimer = 0.0f;
            }

            GL11.glClearColor(Config.BG_COLOR.x, Config.BG_COLOR.y, Config.BG_COLOR.z, Config.BG_COLOR.w);
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