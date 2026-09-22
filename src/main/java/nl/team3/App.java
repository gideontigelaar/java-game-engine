package nl.team3;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryUtil;

import nl.team3.engine.core.Config;
import nl.team3.engine.core.SceneManager;
import nl.team3.engine.input.InputManager;
import nl.team3.engine.input.ActionMap;
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
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

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

        // Input manager setup
        InputManager input = new InputManager(window);
        ActionMap actions = new ActionMap(input);

        actions.bind("pause", GLFW.GLFW_KEY_ESCAPE);
        actions.bind("debugToggle", GLFW.GLFW_KEY_GRAVE_ACCENT);
        actions.bind("lockCursor", GLFW.GLFW_KEY_C);

        boolean[] debugOverlay = {false};
        boolean[] cursorLocked = {false};

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

            if (actions.isActionPressed("pause")) {
                GLFW.glfwSetWindowShouldClose(window, true);
            }

            if (actions.isActionPressed("debugToggle")) {
                debugOverlay[0] = !debugOverlay[0];
                System.out.println("Input debug overlay: " + (debugOverlay[0] ? "ON (F1 to hide)" : "OFF"));
            }

            if (actions.isActionPressed("lockCursor")) {
                cursorLocked[0] = !cursorLocked[0];
                input.setCursorMode(cursorLocked[0] ? GLFW.GLFW_CURSOR_DISABLED : GLFW.GLFW_CURSOR_NORMAL);
            }

            if (debugOverlay[0]) {
                printInputDebug(input, actions);
            }

            input.update();
        }

        sceneManager.cleanup();
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }

    private static void printInputDebug(InputManager input, ActionMap actions) {
        StringBuilder line = new StringBuilder();

        line.append("mouse=(").append((int) input.getMouseX()).append(",").append((int) input.getMouseY()).append(")");
        line.append(" delta=(").append(String.format("%.1f", input.getMouseDeltaX()))
                .append(",").append(String.format("%.1f", input.getMouseDeltaY())).append(")");
        line.append(" inWindow=").append(input.isCursorInWindow());

        if (input.getScrollX() != 0 || input.getScrollY() != 0) {
            line.append(" scroll=(").append(input.getScrollX()).append(",").append(input.getScrollY()).append(")");
        }

        if (input.isButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
            line.append(" LMB");
        }
        if (input.isButtonPressed(GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
            line.append(" RMB-pressed");
        }
        if (input.isButtonReleased(GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
            line.append(" RMB-released");
        }

        if (input.isAnyKeyDown()) {
            line.append(" anyKeyDown");
        }
        if (actions.isActionDown("pause")) {
            line.append(" [pause held]");
        }

        String typed = input.getTextInput();
        if (!typed.isEmpty()) {
            line.append(" typed=\"").append(typed).append("\"");
        }

        System.out.println(line);
    }
}