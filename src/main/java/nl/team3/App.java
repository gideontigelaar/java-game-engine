package nl.team3;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.Config;
import nl.team3.engine.core.SceneManager;
import nl.team3.engine.input.InputManager;
import nl.team3.engine.input.ActionMap;
import nl.team3.games.tictactoe.TicTacToeScene;

import java.nio.IntBuffer;

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

        // Input first: the scene needs these
        InputManager input = new InputManager(window);
        ActionMap actions = new ActionMap(input);
        actions.bind("pause", GLFW.GLFW_KEY_ESCAPE);
        actions.bind("debugToggle", GLFW.GLFW_KEY_GRAVE_ACCENT);
        actions.bind("lockCursor", GLFW.GLFW_KEY_C);

        // Central asset storage
        AssetManager assets = new AssetManager();

        // Create the SceneManager once
        SceneManager sceneManager = new SceneManager(assets);

        // Listen for window resize events
        GLFW.glfwSetFramebufferSizeCallback(window, (win, width, height) -> {
            GL11.glViewport(0, 0, width, height);
            sceneManager.resize(width, height);
        });

        // Set initial viewport and scene size
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            GLFW.glfwGetFramebufferSize(window, pWidth, pHeight);
            GL11.glViewport(0, 0, pWidth.get(0), pHeight.get(0));
            sceneManager.resize(pWidth.get(0), pHeight.get(0));
        }

        sceneManager.changeScene(new TicTacToeScene(input, actions));

        float fpsTimer = 0.0f;
        int frames = 0;

        boolean debugOverlay = false;
        boolean cursorLocked = false;

        double lastTime = GLFW.glfwGetTime();

        while (!GLFW.glfwWindowShouldClose(window)) {
            GLFW.glfwPollEvents();

            double currentTime = GLFW.glfwGetTime();
            float deltaTime = (float) (currentTime - lastTime);
            lastTime = currentTime;

            // Cap delta time
            if (deltaTime > 0.1f) {
                deltaTime = 0.1f;
            }

            frames++;
            fpsTimer += deltaTime;

            if (fpsTimer >= 1.0f) {
                GLFW.glfwSetWindowTitle(window, Config.WINDOW_TITLE + " | FPS: " + frames);
                frames = 0;
                fpsTimer = 0.0f;
            }

            if (actions.isActionPressed("pause")) {

            }
            if (actions.isActionPressed("debugToggle")) {
                debugOverlay = !debugOverlay;
                System.out.println("Input debug overlay: " + (debugOverlay ? "ON (` to hide)" : "OFF"));
            }
            if (actions.isActionPressed("lockCursor")) {
                cursorLocked = !cursorLocked;
                input.setCursorMode(cursorLocked ? GLFW.GLFW_CURSOR_DISABLED : GLFW.GLFW_CURSOR_NORMAL);
            }
            if (debugOverlay) {
                printInputDebug(input, actions);
            }

            GL11.glClearColor(Config.BG_COLOR.x, Config.BG_COLOR.y, Config.BG_COLOR.z, Config.BG_COLOR.w);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            sceneManager.update(deltaTime);
            sceneManager.render();

            GLFW.glfwSwapBuffers(window);

            input.update();
        }

        // First clean Scene, then assets
        sceneManager.cleanup();
        assets.cleanup();

        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }

    private static void printInputDebug(InputManager input, ActionMap actions) {
        StringBuilder line = new StringBuilder();
        boolean hasInput = false;

        if (input.getMouseDeltaX() != 0 || input.getMouseDeltaY() != 0) {
            line.append("mouse=(").append((int) input.getMouseX()).append(",").append((int) input.getMouseY()).append(") ");
            line.append("delta=(").append(String.format("%.1f", input.getMouseDeltaX()))
                    .append(",").append(String.format("%.1f", input.getMouseDeltaY())).append(") ");
            hasInput = true;
        }

        if (input.getScrollX() != 0 || input.getScrollY() != 0) {
            line.append("scroll=(").append(input.getScrollX()).append(",").append(input.getScrollY()).append(") ");
            hasInput = true;
        }

        if (input.isButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
            line.append("LMB ");
            hasInput = true;
        }

        if (input.isButtonPressed(GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
            line.append("RMB-pressed ");
            hasInput = true;
        }

        if (input.isButtonReleased(GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
            line.append("RMB-released ");
            hasInput = true;
        }

        if (input.isAnyKeyDown()) {
            line.append("keyboard_active ");
            hasInput = true;
        }

        for (int i = 0; i <= GLFW.GLFW_KEY_LAST; i++) {
            if (input.isKeyPressed(i)) {
                String keyName = GLFW.glfwGetKeyName(i, 0);
                if (keyName != null) {
                    line.append("[Pressed: ").append(keyName.toUpperCase()).append("] ");
                } else {
                    line.append("[Pressed_ID: ").append(i).append("] ");
                }
                hasInput = true;
            }
        }

        if (actions.isActionDown("pause")) {
            line.append("[pause held] ");
            hasInput = true;
        }

        String typed = input.getTextInput();
        if (!typed.isEmpty()) {
            line.append("typed=\"").append(typed).append("\" ");
            hasInput = true;
        }

        if (hasInput) {
            System.out.println(line.toString().trim());
        }
    }
}