package nl.team3.engine.core;

import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import nl.team3.engine.window.Window;

public record EngineContext(Window window, InputManager input, ActionMap actions, SceneManager sceneManager) {
}
