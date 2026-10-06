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
    private BackgroundRenderer background;

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

    private final InputManager input;
    private final ActionMap actions;
    private final SceneManager sceneManager;

    private final String SERVER_HOST = "127.0.0.1";
    private final int SERVER_PORT = 7789;
    private String statusMessage = "";

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

        textRenderer = assets.getTextRenderer();
        spriteRenderer = assets.getSpriteRenderer();
        background = assets.getBackgroundRenderer();

        mousePointTexture = assets.loadTexture("mousePoint", "/textures/tictactoe/cursorPoint.png");
        mouseGrabTexture = assets.loadTexture("mouseGrab", "/textures/tictactoe/cursorGrab.png");
        input.setCursorVisible(false);
        mousePoint = new Sprite(mousePointTexture);
        mousePoint.setScale(4f);
        mousePoint.setAlpha(1f);

        usernameInput = new TextField("player1", smallFont, new Vector2f(0, 0), new Vector2f(300, 50));
        btnLoginOnline = new Button("LOGIN ONLINE", smallFont, new Vector2f(0, 0), new Vector2f(300, 50));
        btnPlayOffline = new Button("PLAY OFFLINE", smallFont, new Vector2f(0, 0), new Vector2f(300, 50));

        btnLoginOnline.setOnClick(this::attemptLogin);
        btnPlayOffline.setOnClick(() -> {
            sceneManager.changeScene(new TicTacToeScene(input, actions, sceneManager, null));
        });

        resize(currentWidth, currentHeight);
    }

    private void attemptLogin() {
        try {
            ServerConnection connection = new ServerConnection(SERVER_HOST, SERVER_PORT);
            connection.connect();
            connection.sendCommand("login " + usernameInput.getText().toLowerCase());
            sceneManager.changeScene(new LobbyScene(input, actions, sceneManager, connection));
        } catch (Exception e) {
            statusMessage = "Connection failed!";
            System.out.println("Failed to connect to server: " + e.getMessage());
        }
    }

    @Override
    public void update(float dt) {
        mousePosition = new Vector2f((float) input.getMouseX(), (float) input.getMouseY());

        if (input.isButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
            mousePoint.setTexture(mouseGrabTexture);
            mousePoint.setOrigin(0.5f, 0.4f);
        } else {
            mousePoint.setTexture(mousePointTexture);
            mousePoint.setOrigin(0.3f, 0.1f);
        }
        mousePoint.setPosition(mousePosition);

        usernameInput.update(input);
        if (usernameInput.isSubmitPressed()) {
            attemptLogin();
        }

        btnLoginOnline.update(input);
        btnPlayOffline.update(input);
        background.update(dt, true, mousePosition.x, mousePosition.y);
    }

    @Override
    public void render() {
        glClear(GL_COLOR_BUFFER_BIT);
        background.render(currentWidth, currentHeight);

        float titleWidth = gameFont.getTextWidth("TIC TAC TOE");
        textRenderer.drawText(gameFont, "TIC TAC TOE", (currentWidth - titleWidth) / 2f, currentHeight * 0.15f, new Vector4f(1, 1, 1, 1));

        float labelWidth = smallFont.getTextWidth("Username:");
        textRenderer.drawText(smallFont, "Username:", (currentWidth - labelWidth) / 2f, usernameInput.getPosition().y - 15, new Vector4f(0.8f, 0.8f, 0.8f, 1));

        if (!statusMessage.isEmpty()) {
            float statusWidth = smallFont.getTextWidth(statusMessage);
            textRenderer.drawText(smallFont, statusMessage, (currentWidth - statusWidth) / 2f, currentHeight * 0.7f, new Vector4f(1, 0, 0, 1));
        }

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
        usernameInput.setPosition(new Vector2f(centerX, height * 0.40f));
        btnLoginOnline.setPosition(new Vector2f(centerX, height * 0.40f + 80f));
        btnPlayOffline.setPosition(new Vector2f(centerX, height * 0.40f + 160f));
    }
}