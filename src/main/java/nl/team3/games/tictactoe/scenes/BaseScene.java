package nl.team3.games.tictactoe.scenes;

import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.Config;
import nl.team3.engine.core.Scene;
import nl.team3.engine.core.SceneManager;
import nl.team3.engine.graphics.*;
import nl.team3.engine.graphics.animation.Animation;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import nl.team3.games.tictactoe.BackgroundRenderer;
import nl.team3.games.tictactoe.network.ServerConnection;
import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_2;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

public abstract class BaseScene implements Scene {
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
    protected Vector2f mousePosition = new Vector2f();

    private Animation mouseGrabAnim;
    private Animation mouseReleaseAnim;

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

        mousePointTexture = assets.loadTexture("mousePoint", "/textures/tictactoe/cursorPoint.png");
        mouseGrabTexture = assets.loadTexture("mouseGrab", "/textures/tictactoe/cursorGrab.png");
        input.setCursorVisible(false);
        mousePoint = new Sprite(mousePointTexture);
        mousePoint.setScale(2.5f);
        mousePoint.setAlpha(1f);

        mouseGrabAnim = Animation.builder().sprite(mousePoint).scale(new Vector2f(2.5f, 2.5f), new Vector2f(2.0f, 2.0f)).duration(0.3f).easing("ExponentialOut").build();
        mouseReleaseAnim = Animation.builder().sprite(mousePoint).scale(new Vector2f(2.0f, 2.0f), new Vector2f(2.5f, 2.5f)).duration(0.3f).easing("ExponentialOut").build();

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
            mousePoint.setOrigin(0.5f, 0.4f);
            mouseGrabAnim.UpdateAnimation(dt);
            mouseGrabAnim.setScale();
        } else {
            mousePoint.setTexture(mousePointTexture);
            mousePoint.setOrigin(0.3f, 0.1f);
            mouseReleaseAnim.UpdateAnimation(dt);
            mouseReleaseAnim.setScale();
        }
        mousePoint.setPosition(mousePosition);

        if (!input.isTextInputActive() && input.isKeyPressed(GLFW_KEY_2)) {
            background.nextPalette(mousePosition.x, mousePosition.y);
        }

        background.update(dt);

        onUpdate(dt);
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

    protected abstract void onInit(AssetManager assets);
    protected abstract void onUpdate(float dt);
    protected abstract void onRender();
    protected abstract void onResize(int width, int height);
}