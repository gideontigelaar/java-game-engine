package nl.team3.engine.ui;

import nl.team3.engine.graphics.Font;
import nl.team3.engine.graphics.NineSlice;
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
    private Sprite pressedSprite;
    private NineSlice nineSlice;
    private int pixelSize;
    private float pressedTextOffset;
    private float scale = 1f;
    private boolean enabled = true;
    private Runnable onClick;

    private boolean isHovered;
    private boolean isPressed;
    private boolean isClicked;

    private Vector4f normalColor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private Vector4f hoverColor = new Vector4f(0.8f, 0.8f, 0.0f, 1.0f);
    private Vector4f pressedColor = new Vector4f(1.0f, 0.5f, 0.0f, 1.0f);
    private Vector4f disabledColor = new Vector4f(1.0f, 1.0f, 1.0f, 0.5f);

    public Button(String text, Font font, Vector2f position, Vector2f size) {
        this.text = text;
        this.font = font;
        this.position = position;
        this.size = size;
    }

    public void update(InputManager input) {
        isClicked = false;
        if (!enabled) {
            isHovered = false;
            isPressed = false;
            return;
        }

        double mouseX = input.getMouseX();
        double mouseY = input.getMouseY();

        // hit test
        isHovered = mouseX >= position.x && mouseX <= position.x + size.x &&
                mouseY >= position.y && mouseY <= position.y + size.y;

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
        if (scale <= 0f) {
            return;
        }

        Vector4f currentColor = normalColor;
        if (!enabled) {
            currentColor = disabledColor;
        } else if (isPressed) {
            currentColor = pressedColor;
        } else if (isHovered) {
            currentColor = hoverColor;
        }

        // Scale around the center
        float width = size.x * scale;
        float height = size.y * scale;
        float x = position.x + (size.x - width) / 2.0f;
        float y = position.y + (size.y - height) / 2.0f;

        // background sprite / nine-slice rendering
        Sprite background = isPressed && pressedSprite != null ? pressedSprite : backgroundSprite;
        if (background != null && spriteRenderer != null) {
            background.setOrigin(0f, 0f);
            if (nineSlice != null) {
                spriteRenderer.drawNineSlice(background, x, y, width, height, nineSlice, pixelSize * scale);
            } else {
                background.setPosition(x, y);
                background.setScale(
                        width / background.getTexture().getWidth(),
                        height / background.getTexture().getHeight());
                spriteRenderer.draw(background);
            }
        }

        // text rendering
        if (text != null && font != null && textRenderer != null) {
            float textWidth = font.getTextWidth(text);
            float textX = Math.round(position.x + (size.x - textWidth) / 2.0f);
            float textY = Math.round(position.y + (size.y + font.getCapHeight()) / 2.0f);
            if (isPressed) {
                textY += pressedTextOffset;
            }
            float centerX = position.x + size.x / 2.0f;
            float centerY = position.y + size.y / 2.0f;
            textRenderer.drawText(font, text,
                    centerX + (textX - centerX) * scale, centerY + (textY - centerY) * scale, scale, currentColor);
        }
    }

    public void setBackgroundSprite(Sprite backgroundSprite) {
        this.backgroundSprite = backgroundSprite;
    }

    public void setPressedSprite(Sprite pressedSprite, float textOffset) {
        this.pressedSprite = pressedSprite;
        this.pressedTextOffset = textOffset;
    }

    public void setNineSlice(NineSlice nineSlice, int pixelSize) {
        this.nineSlice = nineSlice;
        this.pixelSize = pixelSize;
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

    public boolean isPressed() {
        return isPressed;
    }

    public Vector2f getPosition() {
        return position;
    }

    public void setPosition(Vector2f position) {
        this.position = position;
    }

    public void setPosition(float x, float y) {
        this.position.set(x, y);
    }

    public Vector2f getSize() {
        return size;
    }

    public void setSize(Vector2f size) {
        this.size = size;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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