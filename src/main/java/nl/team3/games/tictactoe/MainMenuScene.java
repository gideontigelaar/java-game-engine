package nl.team3.games.tictactoe;

import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.Config;
import nl.team3.engine.core.Scene;
import nl.team3.engine.core.SceneManager;
import nl.team3.engine.graphics.*;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import nl.team3.engine.ui.Button;
import org.joml.Vector2f;
import org.joml.Vector4f;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

public class MainMenuScene implements Scene {
    private SpriteRenderer spriteRenderer;
    private TextRenderer textRenderer;
    private Font gameFont;
    private Button startButton;

    private int currentHeight = Config.WINDOW_HEIGHT;
    private int currentWidth = Config.WINDOW_WIDTH;

    // mouse
    private Texture mousePointTexture;
    private Texture mouseGrabTexture;
    private Sprite mousePoint;
    private Vector2f mousePosition;

    private BackgroundRenderer background;

    private final InputManager input;
    private final ActionMap actions;
    private final SceneManager sceneManager;

    public MainMenuScene(InputManager input, ActionMap actions, SceneManager sceneManager) {
        this.input = input;
        this.actions = actions;
        this.sceneManager = sceneManager;
    }

    @Override
    public void init(AssetManager assets) {
        System.out.println("MainMenuScene loaded");

        gameFont = assets.loadFont("gameFont", "/fonts/3x5-Microfont-Mono.ttf", 48f);
        textRenderer = new TextRenderer(assets);
        spriteRenderer = new SpriteRenderer(assets);

        // init background
        background = new BackgroundRenderer(assets);

        // init mouse
        mousePointTexture = assets.loadTexture("mousePoint", "/textures/tictactoe/cursorPoint.png");
        mouseGrabTexture = assets.loadTexture("mouseGrab", "/textures/tictactoe/cursorGrab.png");
        input.setCursorVisible(false);
        mousePoint = new Sprite(mousePointTexture);
        mousePoint.setScale(4f);
        mousePoint.setAlpha(1f);

        // init start button
        float buttonWidth = 300f;
        float buttonHeight = 60f;
        float buttonX = (currentWidth - buttonWidth) / 2f;
        float buttonY = (currentHeight - buttonHeight) / 2f;

        startButton = new Button("START GAME", gameFont, new Vector2f(buttonX, buttonY), new Vector2f(buttonWidth, buttonHeight));
        startButton.setOnClick(() -> {
            sceneManager.changeScene(new TicTacToeScene(input, actions));
        });
    }

    @Override
    public void update(float dt) {
        // mouse logic
        mousePosition = new Vector2f(
                (Math.round(input.getMouseX() / 4) + 4f) * 4,
                (Math.round(input.getMouseY() / 4) + 16f) * 4);

        if (input.isButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
            mousePoint.setTexture(mouseGrabTexture);
        } else {
            mousePoint.setTexture(mousePointTexture);
        }
        mousePoint.setPosition(mousePosition);

        // button logic
        startButton.update(input);

        // background update
        background.update(dt, true, mousePosition.x, mousePosition.y);
    }

    @Override
    public void render() {
        glClear(GL_COLOR_BUFFER_BIT);

        background.render(currentWidth, currentHeight);

        // title render
        float titleWidth = gameFont.getTextWidth("TIC TAC TOE");
        float titleX = (currentWidth - titleWidth) / 2f;
        textRenderer.drawText(gameFont, "TIC TAC TOE", titleX, 150, new Vector4f(1, 1, 1, 1));

        // button render
        startButton.render(spriteRenderer, textRenderer);

        // cursor render
        spriteRenderer.draw(mousePoint);
    }

    @Override
    public void resize(int width, int height) {
        this.currentWidth = width;
        this.currentHeight = height;
        spriteRenderer.setProjection(width, height);
        textRenderer.setProjection(width, height);

        float buttonWidth = 300f;
        float buttonHeight = 60f;
        float buttonX = (width - buttonWidth) / 2f;
        float buttonY = (height - buttonHeight) / 2f;
        startButton.setPosition(new Vector2f(buttonX, buttonY));
    }

    @Override
    public void cleanup() {
        System.out.println("MainMenuScene closed");
        background.cleanup();
        textRenderer.cleanup();
    }
}