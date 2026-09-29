package nl.team3.engine.core;

import nl.team3.engine.assets.AssetManager;

public class SceneManager {
    private final AssetManager assets;
    private Scene currentScene;
    private int width;
    private int height;

    public SceneManager(AssetManager assets) {
        this.assets = assets;
    }

    public void changeScene(Scene newScene) {
        if (currentScene != null) {
            currentScene.cleanup();
        }

        currentScene = newScene;
        currentScene.init(assets);

        // Give new scene current window size
        if (width > 0 && height > 0) {
            currentScene.resize(width, height);
        }
    }

    public void update(float dt) {
        if (currentScene != null) {
            currentScene.update(dt);
        }
    }

    public void render() {
        if (currentScene != null) {
            currentScene.render();
        }
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;

        if (currentScene != null) {
            currentScene.resize(width, height);
        }
    }

    public void cleanup() {
        if (currentScene != null) {
            currentScene.cleanup();
            currentScene = null;
        }
    }
}