package nl.team3.engine.ui;

import nl.team3.engine.graphics.Font;
import nl.team3.engine.graphics.Sprite;
import nl.team3.engine.graphics.SpriteRenderer;
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
    private boolean submitPressed;
    private Sprite backgroundSprite;

    public TextField(String defaultText, Font font, Vector2f position, Vector2f size) {
        this.text = defaultText;
        this.font = font;
        this.position = position;
        this.size = size;
    }

    public void update(InputManager input) {
        double mouseX = input.getMouseX();
        double mouseY = input.getMouseY();
        submitPressed = false;

        boolean hovered = mouseX >= position.x && mouseX <= position.x + size.x && mouseY >= position.y && mouseY <= position.y + size.y;

        if (input.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT)) {
            isFocused = hovered;
        }

        if (isFocused) {
            input.requestTextInput();
            text += input.getTextInput();
            if (input.isKeyPressed(GLFW_KEY_BACKSPACE) && text.length() > 0) {
                text = text.substring(0, text.length() - 1);
            }
            if (input.isKeyPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER) || input.isKeyPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER)) {
                submitPressed = true;
            }
        }
    }

    public void render(SpriteRenderer spriteRenderer, TextRenderer textRenderer) {
        if (backgroundSprite != null && spriteRenderer != null) {
            backgroundSprite.setOrigin(0f, 0f);
            backgroundSprite.setPosition(position);
            backgroundSprite.setScale(
                    size.x / backgroundSprite.getTexture().getWidth(),
                    size.y / backgroundSprite.getTexture().getHeight());
            spriteRenderer.draw(backgroundSprite);
        }

        String cursor = (isFocused && (System.currentTimeMillis() % 1000 < 500)) ? "_" : "";
        String displayText = (isFocused ? "> " : "  ") + text + cursor;

        Vector4f color = isFocused ? new Vector4f(1.0f, 1.0f, 0.0f, 1.0f) : new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        float textY = position.y + (size.y / 2.0f) + 8.0f;
        textRenderer.drawText(font, displayText, position.x + 15f, textY, color);
    }

    public void setBackgroundSprite(Sprite backgroundSprite) {
        this.backgroundSprite = backgroundSprite;
    }

    public String getText() {
        return text;
    }

    public void setPosition(Vector2f position) {
        this.position = position;
    }

    public Vector2f getPosition() {
        return position;
    }

    public Vector2f getSize() {
        return size;
    }

    public boolean isSubmitPressed() {
        return submitPressed;
    }
}