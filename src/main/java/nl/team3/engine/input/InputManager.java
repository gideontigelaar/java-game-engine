package nl.team3.engine.input;

import org.lwjgl.glfw.GLFW;
import java.util.Arrays;

public class InputManager {

    private static final int MAX_KEYS = GLFW.GLFW_KEY_LAST + 1;
    private static final int MAX_BUTTONS = GLFW.GLFW_MOUSE_BUTTON_LAST + 1;

    private final long window;

    private final boolean[] keysDown = new boolean[MAX_KEYS];
    private final boolean[] keysDownLastFrame = new boolean[MAX_KEYS];
    private final boolean[] buttonsDown = new boolean[MAX_BUTTONS];
    private final boolean[] buttonsDownLastFrame = new boolean[MAX_BUTTONS];

    private double mouseX, mouseY;
    private double lastMouseX, lastMouseY;
    private double scrollX, scrollY;
    private double scrollXThisFrame, scrollYThisFrame;

    private final StringBuilder textInput = new StringBuilder();

    private boolean cursorInWindow = true;

    public InputManager(long window) {
        this.window = window;

        GLFW.glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if (key < 0 || key >= MAX_KEYS) {
                return; // Ignore unknown keys
            }
            if (action == GLFW.GLFW_PRESS) {
                keysDown[key] = true;
            } else if (action == GLFW.GLFW_RELEASE) {
                keysDown[key] = false;
            }
            // Ignore GLFW_REPEAT
        });

        GLFW.glfwSetMouseButtonCallback(window, (win, button, action, mods) -> {
            if (button < 0 || button >= MAX_BUTTONS) {
                return;
            }
            if (action == GLFW.GLFW_PRESS) {
                buttonsDown[button] = true;
            } else if (action == GLFW.GLFW_RELEASE) {
                buttonsDown[button] = false;
            }
        });

        GLFW.glfwSetCursorPosCallback(window, (win, xpos, ypos) -> {
            mouseX = xpos;
            mouseY = ypos;
        });

        GLFW.glfwSetScrollCallback(window, (win, xoffset, yoffset) -> {
            // Accumulate scroll events
            scrollXThisFrame += xoffset;
            scrollYThisFrame += yoffset;
        });

        GLFW.glfwSetCharCallback(window, (win, codepoint) -> {
            textInput.appendCodePoint(codepoint);
        });

        GLFW.glfwSetCursorEnterCallback(window, (win, entered) -> {
            cursorInWindow = entered;
        });

        // Release keys on focus loss to prevent stuck keys
        GLFW.glfwSetWindowFocusCallback(window, (win, focused) -> {
            if (!focused) {
                releaseAll();
            }
        });
    }

    public void update() {
        System.arraycopy(keysDown, 0, keysDownLastFrame, 0, MAX_KEYS);
        System.arraycopy(buttonsDown, 0, buttonsDownLastFrame, 0, MAX_BUTTONS);

        lastMouseX = mouseX;
        lastMouseY = mouseY;

        scrollX = scrollXThisFrame;
        scrollY = scrollYThisFrame;
        scrollXThisFrame = 0;
        scrollYThisFrame = 0;

        textInput.setLength(0);
    }

    // Release all tracked keys and buttons
    private void releaseAll() {
        Arrays.fill(keysDown, false);
        Arrays.fill(buttonsDown, false);
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

    public boolean isAnyKeyDown() {
        for (boolean down : keysDown) {
            if (down) {
                return true;
            }
        }
        return false;
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

    public double getMouseX() {
        return mouseX;
    }

    public double getMouseY() {
        return mouseY;
    }

    public double getMouseDeltaX() {
        return mouseX - lastMouseX;
    }

    public double getMouseDeltaY() {
        return mouseY - lastMouseY;
    }

    public double getScrollX() {
        return scrollX;
    }

    public double getScrollY() {
        return scrollY;
    }

    // Returns text typed this frame
    public String getTextInput() {
        return textInput.toString();
    }

    public boolean isCursorInWindow() {
        return cursorInWindow;
    }

    // Toggles OS cursor visibility
    public void setCursorMode(int glfwCursorMode) {
        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, glfwCursorMode);
    }
}