package nl.team3.games.tictactoe;

import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.Config;
import nl.team3.engine.core.Scene;
import nl.team3.engine.core.SceneManager;
import nl.team3.engine.graphics.*;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import nl.team3.engine.ui.Button;
import nl.team3.engine.ui.TextField;
import nl.team3.games.tictactoe.network.ServerConnection;
import org.joml.Vector2f;
import org.joml.Vector4f;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

public class MainMenuScene implements Scene {
    private SpriteRenderer spriteRenderer;
    private TextRenderer textRenderer;
    private Font gameFont;
    private Font smallFont;

    private TextField usernameInput;
    private Button btnLoginOnline;
    private Button btnPlayOffline;

    private int currentHeight = Config.WINDOW_HEIGHT;
    private int currentWidth = Config.WINDOW_WIDTH;

    private Texture mousePointTexture;
    private Texture mouseGrabTexture;
    private Sprite mousePoint;
    private Vector2f mousePosition;
    private BackgroundRenderer background;

    private final InputManager input;
    private final ActionMap actions;
    private final SceneManager sceneManager;

    // Server config
    private final String SERVER_HOST = "127.0.0.1";
    private final int SERVER_PORT = 7789;

    public MainMenuScene(InputManager input, ActionMap actions, SceneManager sceneManager) {
        this.input = input;
        this.actions = actions;
        this.sceneManager = sceneManager;
    }

    @Override
    public void init(AssetManager assets) {
        System.out.println("MainMenuScene loaded");
        gameFont = assets.loadFont("gameFont", "/fonts/3x5-Microfont-Mono.ttf", 48f);
        smallFont = assets.loadFont("smallFont", "/fonts/3x5-Microfont-Mono.ttf", 32f);
        textRenderer = new TextRenderer(assets);
        spriteRenderer = new SpriteRenderer(assets);
        background = new BackgroundRenderer(assets);

        mousePointTexture = assets.loadTexture("mousePoint", "/textures/tictactoe/cursorPoint.png");
        mouseGrabTexture = assets.loadTexture("mouseGrab", "/textures/tictactoe/cursorGrab.png");
        input.setCursorVisible(false);
        mousePoint = new Sprite(mousePointTexture);
        mousePoint.setScale(4f);
        mousePoint.setAlpha(1f);

        float centerX = (currentWidth - 300f) / 2f;

        usernameInput = new TextField("player1", smallFont, new Vector2f(centerX, 250), new Vector2f(300, 50));

        btnLoginOnline = new Button("LOGIN ONLINE", smallFont, new Vector2f(centerX, 330), new Vector2f(300, 50));
        btnPlayOffline = new Button("PLAY OFFLINE", smallFont, new Vector2f(centerX, 410), new Vector2f(300, 50));

        btnLoginOnline.setOnClick(() -> {
            try {
                ServerConnection connection = new ServerConnection(SERVER_HOST, SERVER_PORT);
                connection.connect();
                connection.sendCommand("login " + usernameInput.getText());
                sceneManager.changeScene(new LobbyScene(input, actions, sceneManager, connection));
            } catch (Exception e) {
                System.out.println("Failed to connect to server: " + e.getMessage());
            }
        });

        btnPlayOffline.setOnClick(() -> {
            sceneManager.changeScene(new TicTacToeScene(input, actions, sceneManager, null));
        });
    }

    @Override
    public void update(float dt) {
        mousePosition = new Vector2f(
                (Math.round(input.getMouseX() / 4) + 4f) * 4,
                (Math.round(input.getMouseY() / 4) + 16f) * 4);

        if (input.isButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
            mousePoint.setTexture(mouseGrabTexture);
        } else {
            mousePoint.setTexture(mousePointTexture);
        }
        mousePoint.setPosition(mousePosition);

        usernameInput.update(input);
        btnLoginOnline.update(input);
        btnPlayOffline.update(input);
        background.update(dt, true, mousePosition.x, mousePosition.y);
    }

    @Override
    public void render() {
        glClear(GL_COLOR_BUFFER_BIT);
        background.render(currentWidth, currentHeight);

        float titleWidth = gameFont.getTextWidth("TIC TAC TOE");
        float titleX = (currentWidth - titleWidth) / 2f;
        textRenderer.drawText(gameFont, "TIC TAC TOE", titleX, 150, new Vector4f(1, 1, 1, 1));

        textRenderer.drawText(smallFont, "Username:", (currentWidth - 300f) / 2f, 230, new Vector4f(0.8f, 0.8f, 0.8f, 1));

        usernameInput.render(textRenderer);
        btnLoginOnline.render(spriteRenderer, textRenderer);
        btnPlayOffline.render(spriteRenderer, textRenderer);

        spriteRenderer.draw(mousePoint);
    }

    @Override
    public void resize(int width, int height) {
        this.currentWidth = width;
        this.currentHeight = height;
        spriteRenderer.setProjection(width, height);
        textRenderer.setProjection(width, height);

        float centerX = (width - 300f) / 2f;
        usernameInput.setPosition(new Vector2f(centerX, 250));
        btnLoginOnline.setPosition(new Vector2f(centerX, 330));
        btnPlayOffline.setPosition(new Vector2f(centerX, 410));
    }

    @Override
    public void cleanup() {
        System.out.println("MainMenuScene closed");
        background.cleanup();
        textRenderer.cleanup();
    }
}