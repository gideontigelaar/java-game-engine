package nl.team3.engine.core;

import nl.team3.engine.assets.AssetManager;

public class SceneManager {
    private Scene currentScene;
    private final AssetManager assets;

    public SceneManager(AssetManager assets) {
        this.assets = assets;
    }

    public void changeScene(Scene newScene) {
        if(currentScene != null) {
            currentScene.cleanup();
        }

        currentScene = newScene;
        currentScene.init(assets);
    }

    public void update(float dt) {
        if(currentScene != null) {
            currentScene.update(dt);
        }
    }

    public void render() {
        if(currentScene != null) {
            currentScene.render();
        }
    }

    public void resize(int width, int height) {
        if(currentScene != null) {
            currentScene.resize(width, height);
        }
    }

    public void cleanup() {
        if(currentScene != null) {
            currentScene.cleanup();
        }
    }
}