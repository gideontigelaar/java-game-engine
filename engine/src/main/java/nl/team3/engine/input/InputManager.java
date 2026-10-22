package nl.team3.engine.input;

import nl.team3.engine.window.Viewport;
import nl.team3.engine.window.Window;
import org.lwjgl.glfw.GLFW;
import java.util.Arrays;

public class InputManager {
    private static final int MAX_KEYS = GLFW.GLFW_KEY_LAST + 1;
    private static final int MAX_BUTTONS = GLFW.GLFW_MOUSE_BUTTON_LAST + 1;

    private final Viewport viewport;
    private final boolean[] keysDown = new boolean[MAX_KEYS];
    private final boolean[] keysPressed = new boolean[MAX_KEYS];
    private final boolean[] keysReleased = new boolean[MAX_KEYS];
    private final boolean[] keysRepeated = new boolean[MAX_KEYS];
    private final boolean[] buttonsDown = new boolean[MAX_BUTTONS];
    private final boolean[] buttonsPressed = new boolean[MAX_BUTTONS];
    private final boolean[] buttonsReleased = new boolean[MAX_BUTTONS];

    private double rawMouseX, rawMouseY;
    private double lastRawMouseX, lastRawMouseY;
    private double scrollX, scrollY;
    private final StringBuilder textInput = new StringBuilder();
    private boolean cursorInWindow = true;
    private boolean textInputRequested;
    private boolean textInputActive;

    public InputManager(Window window, Viewport viewport) {
        this.viewport = viewport;
        long handle = window.getHandle();

        GLFW.glfwSetKeyCallback(handle, (win, key, scancode, action, mods) -> {
            if (key < 0 || key >= MAX_KEYS) {
                return;
            }
            if (action == GLFW.GLFW_PRESS) {
                keysDown[key] = true;
                keysPressed[key] = true;
            } else if (action == GLFW.GLFW_RELEASE) {
                keysDown[key] = false;
                keysReleased[key] = true;
            } else if (action == GLFW.GLFW_REPEAT) {
                keysRepeated[key] = true;
            }
        });

        GLFW.glfwSetMouseButtonCallback(handle, (win, button, action, mods) -> {
            if (button < 0 || button >= MAX_BUTTONS) {
                return;
            }
            if (action == GLFW.GLFW_PRESS) {
                buttonsDown[button] = true;
                buttonsPressed[button] = true;
            } else if (action == GLFW.GLFW_RELEASE) {
                buttonsDown[button] = false;
                buttonsReleased[button] = true;
            }
        });

        GLFW.glfwSetCursorPosCallback(handle, (win, xpos, ypos) -> {
            rawMouseX = xpos;
            rawMouseY = ypos;
        });

        GLFW.glfwSetScrollCallback(handle, (win, xoffset, yoffset) -> {
            scrollX += xoffset;
            scrollY += yoffset;
        });

        GLFW.glfwSetCharCallback(handle, (win, codepoint) -> {
            textInput.appendCodePoint(codepoint);
        });

        GLFW.glfwSetCursorEnterCallback(handle, (win, entered) -> {
            cursorInWindow = entered;
        });

        GLFW.glfwSetWindowFocusCallback(handle, (win, focused) -> {
            if (!focused) {
                releaseAll();
            }
        });
    }

    public void update() {
        Arrays.fill(keysPressed, false);
        Arrays.fill(keysReleased, false);
        Arrays.fill(keysRepeated, false);
        Arrays.fill(buttonsPressed, false);
        Arrays.fill(buttonsReleased, false);
        lastRawMouseX = rawMouseX;
        lastRawMouseY = rawMouseY;
        scrollX = 0;
        scrollY = 0;
        textInput.setLength(0);
        textInputActive = textInputRequested;
        textInputRequested = false;
    }

    private void releaseAll() {
        for (int key = 0; key < MAX_KEYS; key++) {
            keysReleased[key] |= keysDown[key];
            keysDown[key] = false;
        }
        for (int button = 0; button < MAX_BUTTONS; button++) {
            buttonsReleased[button] |= buttonsDown[button];
            buttonsDown[button] = false;
        }
    }

    public boolean isKeyDown(int key) { return keysDown[key]; }
    public boolean isKeyPressed(int key) { return keysPressed[key]; }
    public boolean isKeyReleased(int key) { return keysReleased[key]; }

    // Key repeat while held down
    public boolean isKeyRepeated(int key) { return keysRepeated[key]; }

    public boolean isAnyKeyDown() {
        for (boolean down : keysDown) if (down) return true;
        return false;
    }

    public boolean isButtonDown(int button) { return buttonsDown[button]; }
    public boolean isButtonPressed(int button) { return buttonsPressed[button]; }
    public boolean isButtonReleased(int button) { return buttonsReleased[button]; }

    public double getMouseX() { return viewport.toVirtualX(rawMouseX); }
    public double getMouseY() { return viewport.toVirtualY(rawMouseY); }
    public double getMouseDeltaX() { return viewport.deltaToVirtualX(rawMouseX - lastRawMouseX); }
    public double getMouseDeltaY() { return viewport.deltaToVirtualY(rawMouseY - lastRawMouseY); }
    public double getScrollX() { return scrollX; }
    public double getScrollY() { return scrollY; }

    public String getTextInput() { return textInput.toString(); }
    public void requestTextInput() { textInputRequested = true; }
    public boolean isTextInputActive() { return textInputActive; }
    public boolean isCursorInWindow() { return cursorInWindow; }
}