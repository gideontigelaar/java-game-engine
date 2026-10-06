package nl.team3.engine.ui;

import nl.team3.engine.graphics.Font;
import nl.team3.engine.graphics.TextRenderer;
import nl.team3.engine.input.InputManager;
import org.joml.Vector2f;
import org.joml.Vector4f;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class TextField {
    private Vector2f position;
    private Vector2f size;
    private String text;
    private Font font;
    private boolean isFocused;

    public TextField(String defaultText, Font font, Vector2f position, Vector2f size) {
        this.text = defaultText;
        this.font = font;
        this.position = position;
        this.size = size;
    }

    public void update(InputManager input) {
        double mouseX = input.getMouseX();
        double mouseY = input.getMouseY();

        boolean hovered = mouseX >= position.x && mouseX <= position.x + size.x && mouseY >= position.y && mouseY <= position.y + size.y;

        if (input.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT)) {
            isFocused = hovered;
        }

        if (isFocused) {
            text += input.getTextInput();
            if (input.isKeyPressed(GLFW_KEY_BACKSPACE) && text.length() > 0) {
                text = text.substring(0, text.length() - 1);
            }
        }
    }

    public void render(TextRenderer textRenderer) {
        String cursor = (isFocused && (System.currentTimeMillis() % 1000 < 500)) ? "_" : "";
        String displayText = (isFocused ? "> " : "  ") + text + cursor;

        Vector4f color = isFocused ? new Vector4f(1.0f, 1.0f, 0.0f, 1.0f) : new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        textRenderer.drawText(font, displayText, position.x, position.y + (size.y / 2.0f), color);
    }

    public String getText() {
        return text;
    }

    public void setPosition(Vector2f position) {
        this.position = position;
    }
}