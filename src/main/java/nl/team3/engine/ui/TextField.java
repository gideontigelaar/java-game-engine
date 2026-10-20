package nl.team3.engine.ui;

import nl.team3.engine.graphics.Font;
import nl.team3.engine.graphics.NineSlice;
import nl.team3.engine.graphics.Sprite;
import nl.team3.engine.graphics.SpriteRenderer;
import nl.team3.engine.graphics.TextRenderer;
import nl.team3.engine.input.InputManager;
import org.joml.Vector2f;
import org.joml.Vector4f;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class TextField {
    private static final String FOCUS_MARKER = "> ";
    private static final String CURSOR = "_";
    private static final float PADDING = 15f;

    private Vector2f position;
    private Vector2f size;
    private String text;
    private Font font;
    private boolean isFocused;
    private boolean submitPressed;
    private boolean centered;
    private Sprite backgroundSprite;
    private NineSlice nineSlice;
    private int pixelSize;

    private final Vector4f normalColor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private final Vector4f focusedColor = new Vector4f(1.0f, 1.0f, 0.0f, 1.0f);

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
            boolean backspace = input.isKeyPressed(GLFW_KEY_BACKSPACE) || input.isKeyRepeated(GLFW_KEY_BACKSPACE);
            if (backspace && !text.isEmpty()) {
                text = text.substring(0, text.length() - 1);
            }
            if (input.isKeyPressed(GLFW_KEY_ENTER) || input.isKeyPressed(GLFW_KEY_KP_ENTER)) {
                submitPressed = true;
            }
        }
    }

    public void render(SpriteRenderer spriteRenderer, TextRenderer textRenderer) {
        if (backgroundSprite != null && spriteRenderer != null) {
            backgroundSprite.setOrigin(0f, 0f);
            if (nineSlice != null) {
                spriteRenderer.drawNineSlice(backgroundSprite, position.x, position.y, size.x, size.y, nineSlice, pixelSize);
            } else {
                backgroundSprite.setPosition(position);
                backgroundSprite.setScale(
                        size.x / backgroundSprite.getTexture().getWidth(),
                        size.y / backgroundSprite.getTexture().getHeight());
                spriteRenderer.draw(backgroundSprite);
            }
        }

        float markerWidth = font.getTextWidth(FOCUS_MARKER);
        float textX = centered
                ? Math.round(position.x + (size.x - font.getTextWidth(text)) / 2.0f)
                : position.x + PADDING + markerWidth;
        float textY = Math.round(position.y + (size.y + font.getCapHeight()) / 2.0f);
        boolean showCursor = isFocused && (System.currentTimeMillis() % 1000 < 500);

        Vector4f color = isFocused ? focusedColor : normalColor;
        if (isFocused) {
            textRenderer.drawText(font, FOCUS_MARKER, textX - markerWidth, textY, color);
        }
        textRenderer.drawText(font, showCursor ? text + CURSOR : text, textX, textY, color);
    }

    public void setBackgroundSprite(Sprite backgroundSprite) {
        this.backgroundSprite = backgroundSprite;
    }

    public void setNineSlice(NineSlice nineSlice, int pixelSize) {
        this.nineSlice = nineSlice;
        this.pixelSize = pixelSize;
    }

    public void setCentered(boolean centered) {
        this.centered = centered;
    }

    public String getText() {
        return text;
    }

    public void setPosition(Vector2f position) {
        this.position = position;
    }

    public void setPosition(float x, float y) {
        this.position.set(x, y);
    }

    public Vector2f getPosition() {
        return position;
    }

    public Vector2f getSize() {
        return size;
    }

    public void setSize(Vector2f size) {
        this.size = size;
    }

    public boolean isSubmitPressed() {
        return submitPressed;
    }
}