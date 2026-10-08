package nl.team3.engine.ui;

import nl.team3.engine.graphics.Font;
import nl.team3.engine.graphics.Sprite;
import nl.team3.engine.graphics.SpriteRenderer;
import nl.team3.engine.graphics.TextRenderer;
import nl.team3.engine.input.InputManager;
import org.joml.Vector2f;
import org.joml.Vector4f;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class Button {
    private Vector2f position;
    private Vector2f size;
    private String text;
    private Font font;

    private Sprite backgroundSprite;
    private Runnable onClick;

    private boolean isHovered;
    private boolean isPressed;
    private boolean isClicked;

    private Vector4f normalColor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private Vector4f hoverColor = new Vector4f(0.8f, 0.8f, 0.0f, 1.0f);
    private Vector4f pressedColor = new Vector4f(1.0f, 0.5f, 0.0f, 1.0f);

    public Button(String text, Font font, Vector2f position, Vector2f size) {
        this.text = text;
        this.font = font;
        this.position = position;
        this.size = size;
    }

    public void update(InputManager input) {
        double mouseX = input.getMouseX();
        double mouseY = input.getMouseY();

        // hit test
        isHovered = mouseX >= position.x && mouseX <= position.x + size.x &&
                mouseY >= position.y && mouseY <= position.y + size.y;

        isClicked = false;

        if (isHovered) {
            if (input.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT)) {
                isPressed = true;
            }
            if (isPressed && input.isButtonReleased(GLFW_MOUSE_BUTTON_LEFT)) {
                isClicked = true;
                isPressed = false;
                if (onClick != null) {
                    onClick.run();
                }
            }
        } else {
            if (input.isButtonReleased(GLFW_MOUSE_BUTTON_LEFT)) {
                isPressed = false;
            }
        }
    }

    public void render(SpriteRenderer spriteRenderer, TextRenderer textRenderer) {
        Vector4f currentColor = normalColor;
        if (isPressed) {
            currentColor = pressedColor;
        } else if (isHovered) {
            currentColor = hoverColor;
        }

        // background sprite
        if (backgroundSprite != null && spriteRenderer != null) {
            backgroundSprite.setOrigin(0f, 0f);
            backgroundSprite.setPosition(position);
            backgroundSprite.setScale(
                    size.x / backgroundSprite.getTexture().getWidth(),
                    size.y / backgroundSprite.getTexture().getHeight());
            spriteRenderer.draw(backgroundSprite);
        }

        // text rendering
        if (text != null && font != null && textRenderer != null) {
            float textWidth = font.getTextWidth(text);
            float textX = position.x + (size.x - textWidth) / 2.0f;
            float textY = position.y + (size.y / 2.0f) + 12.0f;
            textRenderer.drawText(font, text, textX, textY, currentColor);
        }
    }

    public void setBackgroundSprite(Sprite backgroundSprite) {
        this.backgroundSprite = backgroundSprite;
    }

    public void setOnClick(Runnable onClick) {
        this.onClick = onClick;
    }

    public boolean isClicked() {
        return isClicked;
    }

    public boolean isHovered() {
        return isHovered;
    }

    public Vector2f getPosition() {
        return position;
    }

    public void setPosition(Vector2f position) {
        this.position = position;
    }

    public Vector2f getSize() {
        return size;
    }

    public void setSize(Vector2f size) {
        this.size = size;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setColors(Vector4f normal, Vector4f hover, Vector4f pressed) {
        this.normalColor = normal;
        this.hoverColor = hover;
        this.pressedColor = pressed;
    }
}