package nl.team3.games.tictactoe.scenes;

import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.Config;
import nl.team3.engine.core.Scene;
import nl.team3.engine.core.SceneManager;
import nl.team3.engine.graphics.*;
import nl.team3.engine.graphics.animation.Animation;
import nl.team3.engine.graphics.animation.SpriteAnimation;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import nl.team3.engine.ui.Button;
import nl.team3.games.tictactoe.BackgroundRenderer;
import nl.team3.games.tictactoe.network.ServerConnection;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.function.Supplier;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_2;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

public abstract class BaseScene implements Scene {
    // UI art is drawn at 4x, button size and spacing are shared by all scenes
    protected static final int UI_SCALE = 4;
    protected static final float UI_SPACING = 4 * UI_SCALE;
    protected static final float BUTTON_WIDTH = 300f;
    protected static final float SMALL_BUTTON_WIDTH = 200f;
    protected static final float BUTTON_HEIGHT = 68f;

    private static final NineSlice BUTTON_SLICE = new NineSlice(5, 6, 5, 5);
    private static final float TRANSITION_TIME = 0.7f;
    private static final float PALETTE_SWITCH_TIME = 0.2f;

    protected final InputManager input;
    protected final ActionMap actions;
    protected final SceneManager sceneManager;
    protected final ServerConnection connection;

    protected SpriteRenderer spriteRenderer;
    protected TextRenderer textRenderer;
    protected BackgroundRenderer background;

    protected Font gameFont;
    protected Font smallFont;

    protected int currentWidth = Config.WINDOW_WIDTH;
    protected int currentHeight = Config.WINDOW_HEIGHT;

    private Texture mousePointTexture;
    private Texture mouseGrabTexture;
    private Sprite mousePoint;

    private Texture buttonTexture;
    private Texture buttonPressedTexture;
    private Texture logoTexture;

    protected Vector2f mousePosition = new Vector2f();

    private SpriteAnimation mouseGrabAnim;
    private SpriteAnimation mouseReleaseAnim;

    private Supplier<Scene> nextScene;
    private int nextPalette;
    private float transitionTime;
    private Button[] shrinkingButtons;
    private Animation[] shrinkAnimations;

    public BaseScene(InputManager input, ActionMap actions, SceneManager sceneManager, ServerConnection connection) {
        this.input = input;
        this.actions = actions;
        this.sceneManager = sceneManager;
        this.connection = connection;
    }

    @Override
    public final void init(AssetManager assets) {
        textRenderer = assets.getTextRenderer();
        spriteRenderer = assets.getSpriteRenderer();
        background = assets.getBackgroundRenderer();

        gameFont = assets.loadFont("gameFont", "/fonts/3x5-Microfont-Mono.ttf", 48f);
        smallFont = assets.loadFont("smallFont", "/fonts/3x5-Microfont-Mono.ttf", 24f);

        buttonTexture = assets.loadTexture("btnTexture", "/textures/tictactoe/button.png");
        buttonPressedTexture = assets.loadTexture("btnPressedTexture", "/textures/tictactoe/buttonPressed.png");
        logoTexture = assets.loadTexture("tripleTlogo", "/textures/tictactoe/tripleTlogo.png");

        mousePointTexture = assets.loadTexture("mousePoint", "/textures/tictactoe/cursorPoint.png");
        mouseGrabTexture = assets.loadTexture("mouseGrab", "/textures/tictactoe/cursorGrab.png");
        input.setCursorVisible(false);

        mousePoint = new Sprite(mousePointTexture);
        mousePoint.setScale(4f);
        mousePoint.setAlpha(1f);

        mouseGrabAnim = SpriteAnimation.builder().sprite(mousePoint).scale(new Vector2f(4f, 4f), new Vector2f(3f, 3f)).duration(0.3f).easing("ExponentialOut").build();
        mouseReleaseAnim = SpriteAnimation.builder().sprite(mousePoint).scale(new Vector2f(3f, 3f), new Vector2f(4f, 4f)).duration(0.3f).easing("ExponentialOut").build();

        onInit(assets);
        resize(currentWidth, currentHeight);
    }

    @Override
    public final void update(float dt) {
        mousePosition.set((float) input.getMouseX(), (float) input.getMouseY());

        if (input.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT)) mouseGrabAnim.startAnimation();
        if (input.isButtonReleased(GLFW_MOUSE_BUTTON_LEFT)) mouseReleaseAnim.startAnimation();

        if (input.isButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
            mousePoint.setTexture(mouseGrabTexture);
            //mousePoint.setOrigin(0.5f, 0.4f);
            mouseGrabAnim.UpdateAnimation(dt);
            mouseGrabAnim.setScale();
        } else {
            mousePoint.setTexture(mousePointTexture);
            //mousePoint.setOrigin(0.3f, 0.1f);
            mouseReleaseAnim.UpdateAnimation(dt);
            mouseReleaseAnim.setScale();
        }
        mousePoint.setPosition(mousePosition.x + 4f, mousePosition.y + 20f);

        if (!input.isTextInputActive() && input.isKeyPressed(GLFW_KEY_2)) {
            background.nextPalette(mousePosition.x, mousePosition.y);
        }

        background.update(dt);

        onUpdate(dt);

        if (isTransitioning()) {
            updateTransition(dt);
        }
    }

    @Override
    public final void render() {
        glClear(GL_COLOR_BUFFER_BIT);
        background.render(currentWidth, currentHeight);

        onRender();

        spriteRenderer.draw(mousePoint);
    }

    @Override
    public final void resize(int width, int height) {
        this.currentWidth = width;
        this.currentHeight = height;
        spriteRenderer.setProjection(width, height);
        textRenderer.setProjection(width, height);

        onResize(width, height);
    }

    // Button in the same style as the main menu buttons
    protected Button createButton(String text, float width, Runnable onClick) {
        Button button = new Button(text, smallFont, new Vector2f(), new Vector2f(width, BUTTON_HEIGHT));
        button.setBackgroundSprite(new Sprite(buttonTexture));
        button.setPressedSprite(new Sprite(buttonPressedTexture), UI_SCALE);
        button.setNineSlice(BUTTON_SLICE, UI_SCALE);
        button.setOnClick(onClick);
        return button;
    }

    protected Sprite createLogo() {
        Sprite logo = new Sprite(logoTexture);
        logo.setScale(UI_SCALE);
        return logo;
    }

    // Text centered horizontally, y is the top of the capitals
    protected void drawCenteredText(Font font, String text, float y, Vector4f color) {
        float x = Math.round((currentWidth - font.getTextWidth(text)) / 2f);
        textRenderer.drawText(font, text, x, Math.round(y + font.getCapHeight()), color);
    }

    // Shrink the buttons and sweep in a new background color, then change scene
    protected void transitionTo(Supplier<Scene> scene, int palette, Button... buttons) {
        nextScene = scene;
        nextPalette = palette;
        transitionTime = 0f;
        shrinkingButtons = buttons;
        shrinkAnimations = new Animation[buttons.length];
        for (int i = 0; i < buttons.length; i++) {
            // Last button disappears first
            shrinkAnimations[i] = new Animation(1f, 0f, "ExponentialIn", 0.3f + 0.1f * (buttons.length - 1 - i));
            shrinkAnimations[i].startAnimation();
        }
    }

    protected boolean isTransitioning() {
        return nextScene != null;
    }

    private void updateTransition(float dt) {
        float previousTime = transitionTime;
        transitionTime += dt;

        for (int i = 0; i < shrinkingButtons.length; i++) {
            shrinkAnimations[i].UpdateAnimation(dt);
            shrinkingButtons[i].setScale(shrinkAnimations[i].getValue());
        }

        if (previousTime < PALETTE_SWITCH_TIME && transitionTime >= PALETTE_SWITCH_TIME) {
            background.setPalette(0, 0, nextPalette);
        }

        if (transitionTime >= TRANSITION_TIME) {
            sceneManager.changeScene(nextScene.get());
        }
    }

    protected abstract void onInit(AssetManager assets);
    protected abstract void onUpdate(float dt);
    protected abstract void onRender();
    protected abstract void onResize(int width, int height);
}