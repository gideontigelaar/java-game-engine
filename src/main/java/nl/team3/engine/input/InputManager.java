package nl.team3.engine.input;

import nl.team3.engine.core.Viewport;
import org.lwjgl.glfw.GLFW;
import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.*;

public class InputManager {
    private static final int MAX_KEYS = GLFW.GLFW_KEY_LAST + 1;
    private static final int MAX_BUTTONS = GLFW.GLFW_MOUSE_BUTTON_LAST + 1;

    private final long window;
    private final Viewport viewport;
    private final boolean[] keysDown = new boolean[MAX_KEYS];
    private final boolean[] keysDownLastFrame = new boolean[MAX_KEYS];
    private final boolean[] buttonsDown = new boolean[MAX_BUTTONS];
    private final boolean[] buttonsDownLastFrame = new boolean[MAX_BUTTONS];

    private double rawMouseX, rawMouseY;
    private double lastRawMouseX, lastRawMouseY;
    private double scrollX, scrollY;
    private final StringBuilder textInput = new StringBuilder();
    private boolean cursorInWindow = true;
    private boolean textInputRequested;
    private boolean textInputActive;

    public InputManager(long window, Viewport viewport) {
        this.window = window;
        this.viewport = viewport;

        GLFW.glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if (key < 0 || key >= MAX_KEYS) {
                return;
            }
            if (action == GLFW.GLFW_PRESS) {
                keysDown[key] = true;
            } else if (action == GLFW.GLFW_RELEASE) {
                keysDown[key] = false;
            }
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
            rawMouseX = xpos;
            rawMouseY = ypos;
        });

        GLFW.glfwSetScrollCallback(window, (win, xoffset, yoffset) -> {
            scrollX += xoffset;
            scrollY += yoffset;
        });

        GLFW.glfwSetCharCallback(window, (win, codepoint) -> {
            textInput.appendCodePoint(codepoint);
        });

        GLFW.glfwSetCursorEnterCallback(window, (win, entered) -> {
            cursorInWindow = entered;
        });

        GLFW.glfwSetWindowFocusCallback(window, (win, focused) -> {
            if (!focused) {
                releaseAll();
            }
        });
    }

    public void update() {
        System.arraycopy(keysDown, 0, keysDownLastFrame, 0, MAX_KEYS);
        System.arraycopy(buttonsDown, 0, buttonsDownLastFrame, 0, MAX_BUTTONS);
        lastRawMouseX = rawMouseX;
        lastRawMouseY = rawMouseY;
        scrollX = 0;
        scrollY = 0;
        textInput.setLength(0);
        textInputActive = textInputRequested;
        textInputRequested = false;
    }

    private void releaseAll() {
        Arrays.fill(keysDown, false);
        Arrays.fill(buttonsDown, false);
    }

    public boolean isKeyDown(int key) { return keysDown[key]; }
    public boolean isKeyPressed(int key) { return keysDown[key] && !keysDownLastFrame[key]; }
    public boolean isKeyReleased(int key) { return !keysDown[key] && keysDownLastFrame[key]; }

    public boolean isAnyKeyDown() {
        for (boolean down : keysDown) if (down) return true;
        return false;
    }

    public boolean isButtonDown(int button) { return buttonsDown[button]; }
    public boolean isButtonPressed(int button) { return buttonsDown[button] && !buttonsDownLastFrame[button]; }
    public boolean isButtonReleased(int button) { return !buttonsDown[button] && buttonsDownLastFrame[button]; }

    public double getMouseX() { return viewport.toVirtualX(rawMouseX); }
    public double getMouseY() { return viewport.toVirtualY(rawMouseY); }
    public double getMouseDeltaX() { return viewport.deltaToVirtualX(rawMouseX - lastRawMouseX); }
    public double getMouseDeltaY() { return viewport.deltaToVirtualY(rawMouseY - lastRawMouseY); }
    public double getScrollX() { return scrollX; }
    public double getScrollY() { return scrollY; }

    public void setCursorVisible(boolean visible) {
        glfwSetInputMode(window, GLFW_CURSOR, visible ? GLFW_CURSOR_NORMAL : GLFW_CURSOR_HIDDEN);
    }

    public String getTextInput() { return textInput.toString(); }
    public void requestTextInput() { textInputRequested = true; }
    public boolean isTextInputActive() { return textInputActive; }
    public boolean isCursorInWindow() { return cursorInWindow; }

    public void setCursorMode(int glfwCursorMode) {
        glfwSetInputMode(window, GLFW.GLFW_CURSOR, glfwCursorMode);
    }

    public void requestClose() {
        glfwSetWindowShouldClose(window, true);
    }
}