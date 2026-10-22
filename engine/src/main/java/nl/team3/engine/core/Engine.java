package nl.team3.engine.core;

import org.joml.Vector4fc;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;

import java.nio.IntBuffer;

public final class Engine {
    @FunctionalInterface
    public interface SceneFactory {
        Scene create(InputManager input, ActionMap actions, SceneManager sceneManager);
    }

    private Engine() {
    }

    public static void run(EngineConfig config, SceneFactory firstScene) {
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

        long window = GLFW.glfwCreateWindow(config.windowWidth(), config.windowHeight(), config.title(), MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create GLFW window");
        }
        fitAndCenterWindow(window, config.windowWidth(), config.windowHeight());
        GLFW.glfwSetWindowSizeLimits(window, config.minWindowWidth(), config.minWindowHeight(), GLFW.GLFW_DONT_CARE, GLFW.GLFW_DONT_CARE);

        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(config.vsync() ? 1 : 0);
        GLFW.glfwShowWindow(window);

        GL.createCapabilities();

        Viewport viewport = new Viewport(config.minWindowWidth(), config.minWindowHeight(),
                config.minGuiWidth(), config.minGuiHeight(), config.guiScale());
        updateViewport(window, viewport);

        // Input first: the scene needs these
        InputManager input = new InputManager(window, viewport);
        ActionMap actions = new ActionMap(input);

        // Central asset storage
        AssetManager assets = new AssetManager();

        // Create the SceneManager once
        SceneManager sceneManager = new SceneManager(assets);

        // Listen for window resize events
        Runnable refreshViewport = () -> {
            updateViewport(window, viewport);
            sceneManager.resize(viewport.getScaledWidth(), viewport.getScaledHeight());
        };
        GLFW.glfwSetFramebufferSizeCallback(window, (win, width, height) -> refreshViewport.run());

        // Set initial viewport and scene size
        sceneManager.resize(viewport.getScaledWidth(), viewport.getScaledHeight());

        sceneManager.changeScene(firstScene.create(input, actions, sceneManager));

        double lastTime = GLFW.glfwGetTime();
        Vector4fc clearColor = config.clearColor();
        GL11.glClearColor(clearColor.x(), clearColor.y(), clearColor.z(), clearColor.w());

        while (!GLFW.glfwWindowShouldClose(window)) {
            GLFW.glfwPollEvents();

            double currentTime = GLFW.glfwGetTime();
            float deltaTime = (float) (currentTime - lastTime);
            lastTime = currentTime;

            // Cap delta time
            if (deltaTime > 0.1f) {
                deltaTime = 0.1f;
            }

            viewport.apply();
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

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

    private static void updateViewport(long window, Viewport viewport) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer winW = stack.mallocInt(1);
            IntBuffer winH = stack.mallocInt(1);
            IntBuffer fbW = stack.mallocInt(1);
            IntBuffer fbH = stack.mallocInt(1);
            GLFW.glfwGetWindowSize(window, winW, winH);
            GLFW.glfwGetFramebufferSize(window, fbW, fbH);
            viewport.update(winW.get(0), winH.get(0), fbW.get(0), fbH.get(0));
        }
    }

    private static void fitAndCenterWindow(long window, int targetWidth, int targetHeight) {
        long monitor = GLFW.glfwGetPrimaryMonitor();
        if (monitor == MemoryUtil.NULL) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer areaX = stack.mallocInt(1);
            IntBuffer areaY = stack.mallocInt(1);
            IntBuffer areaW = stack.mallocInt(1);
            IntBuffer areaH = stack.mallocInt(1);
            GLFW.glfwGetMonitorWorkarea(monitor, areaX, areaY, areaW, areaH);

            IntBuffer left = stack.mallocInt(1);
            IntBuffer top = stack.mallocInt(1);
            IntBuffer right = stack.mallocInt(1);
            IntBuffer bottom = stack.mallocInt(1);
            GLFW.glfwGetWindowFrameSize(window, left, top, right, bottom);

            int frameW = left.get(0) + right.get(0);
            int frameH = top.get(0) + bottom.get(0);

            float fit = Math.min(1f, Math.min(
                    (areaW.get(0) - frameW) / (float) targetWidth,
                    (areaH.get(0) - frameH) / (float) targetHeight));
            int width = Math.max(1, Math.round(targetWidth * fit));
            int height = Math.max(1, Math.round(targetHeight * fit));
            GLFW.glfwSetWindowSize(window, width, height);

            int posX = areaX.get(0) + (areaW.get(0) - frameW - width) / 2 + left.get(0);
            int posY = areaY.get(0) + (areaH.get(0) - frameH - height) / 2 + top.get(0);
            GLFW.glfwSetWindowPos(window, posX, posY);
        }
    }
}
