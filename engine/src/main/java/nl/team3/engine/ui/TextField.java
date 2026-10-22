package nl.team3.engine.ui;

import nl.team3.engine.graphics.Font;
import nl.team3.engine.graphics.NineSlice;
import nl.team3.engine.graphics.Sprite;
import nl.team3.engine.graphics.SpriteRenderer;
import nl.team3.engine.graphics.TextRenderer;
import nl.team3.engine.input.InputManager;
import nl.team3.engine.input.Keys;
import nl.team3.engine.input.MouseButtons;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.function.IntPredicate;

public class TextField {
    private static final String FOCUS_MARKER = "> ";
    private static final String CURSOR = "_";
    private static final float PADDING = 15f;
    private static final float PLACEHOLDER_ALPHA = 0.5f;

    private Vector2f position;
    private Vector2f size;
    private String text;
    private Font font;
    private boolean isFocused;
    private boolean isHovered;
    private boolean submitPressed;
    private boolean centered;
    private Sprite backgroundSprite;
    private NineSlice nineSlice;
    private int pixelSize;

    private String placeholder = "";
    private int maxLength = Integer.MAX_VALUE;
    private IntPredicate allowedChars = c -> true;

    private Vector4f normalColor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private Vector4f focusedColor = new Vector4f(1.0f, 1.0f, 0.0f, 1.0f);
    private Vector4f hoverColor = new Vector4f(0.8f, 0.8f, 0.0f, 1.0f);
    private final Vector4f placeholderColor = new Vector4f();

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

        isHovered = mouseX >= position.x && mouseX <= position.x + size.x && mouseY >= position.y && mouseY <= position.y + size.y;

        if (input.isButtonPressed(MouseButtons.LEFT)) {
            isFocused = isHovered;
        }

        if (isFocused) {
            input.requestTextInput();
            input.getTextInput().codePoints().filter(allowedChars).forEach(this::append);
            boolean backspace = input.isKeyPressed(Keys.BACKSPACE) || input.isKeyRepeated(Keys.BACKSPACE);
            if (backspace && !text.isEmpty()) {
                text = text.substring(0, text.length() - 1);
            }
            if (input.isKeyPressed(Keys.ENTER) || input.isKeyPressed(Keys.KP_ENTER)) {
                submitPressed = true;
            }
        }
    }

    private void append(int codePoint) {
        if (text.length() < maxLength) {
            text += Character.toString(codePoint);
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

        boolean showPlaceholder = text.isEmpty() && !isFocused;
        String shown = showPlaceholder ? placeholder : text;

        float markerWidth = font.getTextWidth(FOCUS_MARKER);
        float textX = centered
                ? Math.round(position.x + (size.x - font.getTextWidth(shown)) / 2.0f)
                : position.x + PADDING + markerWidth;
        float textY = Math.round(position.y + (size.y + font.getCapHeight()) / 2.0f);
        boolean showCursor = isFocused && (System.currentTimeMillis() % 1000 < 500);

        Vector4f color = isFocused ? focusedColor : isHovered ? hoverColor : normalColor;
        if (showPlaceholder) {
            color = placeholderColor.set(color.x, color.y, color.z, color.w * PLACEHOLDER_ALPHA);
        }
        if (isFocused) {
            textRenderer.drawText(font, FOCUS_MARKER, textX - markerWidth, textY, color);
        }
        textRenderer.drawText(font, showCursor ? shown + CURSOR : shown, textX, textY, color);
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

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public void setAllowedChars(IntPredicate allowedChars) {
        this.allowedChars = allowedChars;
    }

    public void setColors(Vector4f normal, Vector4f hover, Vector4f focused) {
        this.normalColor = normal;
        this.hoverColor = hover;
        this.focusedColor = focused;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isFocused() {
        return isFocused;
    }

    public void setFocused(boolean focused) {
        this.isFocused = focused;
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