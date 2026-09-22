package nl.team3.engine.core.InputManager;

import org.lwjgl.glfw.GLFW;

public class InputManager {

    private static final int MAX_KEYS = GLFW.GLFW_KEY_LAST + 1;
    private static final int MAX_BUTTONS = GLFW.GLFW_MOUSE_BUTTON_LAST + 1;

    private final boolean[] keysDown = new boolean[MAX_KEYS];
    private final boolean[] keysDownLastFrame = new boolean[MAX_KEYS];

    private final boolean[] buttonsDown = new boolean[MAX_BUTTONS];
    private final boolean[] buttonsDownLastFrame = new boolean[MAX_BUTTONS];

    private double mouseX, mouseY;
    private double lastMouseX, lastMouseY;
    private double scrollX, scrollY;



    public InputManager (long window){
        GLFW.glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if (key < 0 || key >= MAX_KEYS) return; // GLFW_KEY_UNKNOWN guard
            if (action == GLFW.GLFW_PRESS) keysDown[key] = true;
            else if (action == GLFW.GLFW_RELEASE) keysDown[key] = false;
            // GLFW_REPEAT ignored on purpose — we derive "held" from state, not repeat events
        });

        GLFW.glfwSetMouseButtonCallback(window, (win, button, action, mods) -> {
            if (button < 0 || button >= MAX_BUTTONS) return;
            if (action == GLFW.GLFW_PRESS) buttonsDown[button] = true;
            else if (action == GLFW.GLFW_RELEASE) buttonsDown[button] = false;
        });

        GLFW.glfwSetCursorPosCallback(window, (win, xpos, ypos) -> {
            mouseX = xpos;
            mouseY = ypos;
        });

        GLFW.glfwSetScrollCallback(window, (win, xoffset, yoffset) -> {
            scrollX = xoffset;
            scrollY = yoffset;
        });
    }

    public void update (){
        System.arraycopy(keysDown, 0, keysDownLastFrame, 0, MAX_KEYS);
        System.arraycopy(buttonsDown, 0, buttonsDownLastFrame, 0, MAX_BUTTONS);
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        scrollX = 0;
        scrollY = 0;
    }

    public boolean isKeyDown(int key) {
        return keysDown[key];
    }

    public boolean isKeyPressed(int key) {
        return keysDown[key] && !keysDownLastFrame[key];
    }

    public boolean isKeyReleased(int key) {
        return !keysDown[key] && keysDownLastFrame[key];
    }


    public boolean isButtonDown(int button) {
        return buttonsDown[button];
    }

    public boolean isButtonPressed(int button) {
        return buttonsDown[button] && !buttonsDownLastFrame[button];
    }

    public boolean isButtonReleased(int button) {
        return !buttonsDown[button] && buttonsDownLastFrame[button];
    }

    public double getMouseX() { return mouseX; }
    public double getMouseY() { return mouseY; }
    public double getMouseDeltaX() { return mouseX - lastMouseX; }
    public double getMouseDeltaY() { return mouseY - lastMouseY; }

    public double getScrollX() { return scrollX; }
    public double getScrollY() { return scrollY; }
}
