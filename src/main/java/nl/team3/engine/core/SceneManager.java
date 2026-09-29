package nl.team3.engine.core;

public class SceneManager {
    private Scene currentScene;
    private long window;
    public SceneManager(long window) {

    }

    public void changeScene(Scene newScene) {
        if(currentScene != null) {
            currentScene.cleanup();
        }

        currentScene = newScene;
        currentScene.init();
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

    public void cleanup() {
        if(currentScene != null) {
            currentScene.cleanup();
        }
    }
}