package nl.team3.engine.core;

import nl.team3.engine.assets.AssetManager;

public interface Scene {
    void init(AssetManager assets);

    void update(float dt);

    void render();

    void resize(int width, int height);

    // Free what Scene owns itself, AssetManager does the rest
    default void cleanup() {
    }
}