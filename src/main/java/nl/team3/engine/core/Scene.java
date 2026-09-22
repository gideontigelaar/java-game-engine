package nl.team3.engine.core;

public interface Scene {
    void init();

    void update(float dt);

    void render();

    void cleanup();
}