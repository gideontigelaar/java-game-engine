package nl.team3.engine.core;

import org.joml.Vector4fc;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.Configuration;

import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import nl.team3.engine.window.Viewport;
import nl.team3.engine.window.Window;

public final class Engine {
    @FunctionalInterface
    public interface SceneFactory {
        Scene create(EngineContext context);
    }

    private Engine() {
    }

    public static void run(EngineConfig config, SceneFactory firstScene) {
        Configuration.GLFW_CHECK_THREAD0.set(false);
        GLFWErrorCallback.createPrint(System.err).set();

        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }

        Window window = new Window(config.title(), config.windowWidth(), config.windowHeight(),
                config.minWindowWidth(), config.minWindowHeight(), config.vsync());

        GL.createCapabilities();

        Viewport viewport = new Viewport(config.minWindowWidth(), config.minWindowHeight(),
                config.minGuiWidth(), config.minGuiHeight(), config.guiScale());
        window.updateViewport(viewport);

        // Input first: the scene needs these
        InputManager input = new InputManager(window, viewport);
        ActionMap actions = new ActionMap(input);

        // Central asset storage
        AssetManager assets = new AssetManager();

        // Create the SceneManager once
        SceneManager sceneManager = new SceneManager(assets);

        // Listen for window resize events
        window.onFramebufferResize(() -> {
            window.updateViewport(viewport);
            sceneManager.resize(viewport.getScaledWidth(), viewport.getScaledHeight());
        });

        // Set initial viewport and scene size
        sceneManager.resize(viewport.getScaledWidth(), viewport.getScaledHeight());

        EngineContext context = new EngineContext(window, input, actions, sceneManager);
        sceneManager.changeScene(firstScene.create(context));

        double lastTime = GLFW.glfwGetTime();
        Vector4fc clearColor = config.clearColor();
        GL11.glClearColor(clearColor.x(), clearColor.y(), clearColor.z(), clearColor.w());

        while (!window.shouldClose()) {
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

            window.swapBuffers();

            input.update();
        }

        // First clean Scene, then assets
        sceneManager.cleanup();
        assets.cleanup();

        window.destroy();
        GLFW.glfwTerminate();
        GLFWErrorCallback callback = GLFW.glfwSetErrorCallback(null);
        if (callback != null) {
            callback.free();
        }
    }
}
