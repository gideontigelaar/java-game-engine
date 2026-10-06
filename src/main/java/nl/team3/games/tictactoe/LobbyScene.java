package nl.team3.games.tictactoe;

import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.Config;
import nl.team3.engine.core.Scene;
import nl.team3.engine.core.SceneManager;
import nl.team3.engine.graphics.*;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import nl.team3.engine.ui.Button;
import nl.team3.games.tictactoe.network.ServerConnection;
import org.joml.Vector2f;
import org.joml.Vector4f;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

public class LobbyScene implements Scene {
    private SpriteRenderer spriteRenderer;
    private TextRenderer textRenderer;
    private Font gameFont;
    private Font smallFont;

    private Button btnGetPlayers;
    private Button btnGetGames;
    private Button btnSubscribe;
    private Button btnDisconnect;

    private int currentWidth = Config.WINDOW_WIDTH;
    private int currentHeight = Config.WINDOW_HEIGHT;

    private Texture mousePointTexture;
    private Texture mouseGrabTexture;
    private Sprite mousePoint;
    private Vector2f mousePosition;
    private BackgroundRenderer background;

    private final InputManager input;
    private final ActionMap actions;
    private final SceneManager sceneManager;
    private final ServerConnection connection;

    private String serverStatus = "Awaiting Server Response...";
    private String playersList = "Players: []";
    private String gamesList = "Games: []";

    public LobbyScene(InputManager input, ActionMap actions, SceneManager sceneManager, ServerConnection connection) {
        this.input = input;
        this.actions = actions;
        this.sceneManager = sceneManager;
        this.connection = connection;
    }

    @Override
    public void init(AssetManager assets) {
        gameFont = assets.loadFont("gameFont", "/fonts/3x5-Microfont-Mono.ttf", 48f);
        smallFont = assets.loadFont("smallFont", "/fonts/3x5-Microfont-Mono.ttf", 24f);
        textRenderer = new TextRenderer(assets);
        spriteRenderer = new SpriteRenderer(assets);
        background = new BackgroundRenderer(assets);

        mousePointTexture = assets.loadTexture("mousePoint", "/textures/tictactoe/cursorPoint.png");
        mouseGrabTexture = assets.loadTexture("mouseGrab", "/textures/tictactoe/cursorGrab.png");
        input.setCursorVisible(false);
        mousePoint = new Sprite(mousePointTexture);
        mousePoint.setScale(4f);

        btnGetPlayers = new Button("GET PLAYERS", smallFont, new Vector2f(50, 600), new Vector2f(180, 50));
        btnGetGames = new Button("GET GAMES", smallFont, new Vector2f(250, 600), new Vector2f(180, 50));
        btnSubscribe = new Button("FIND MATCH", smallFont, new Vector2f(450, 600), new Vector2f(180, 50));
        btnDisconnect = new Button("DISCONNECT", smallFont, new Vector2f(250, 660), new Vector2f(180, 50));

        btnGetPlayers.setOnClick(() -> connection.sendCommand("get playerlist"));
        btnGetGames.setOnClick(() -> connection.sendCommand("get gamelist"));
        btnSubscribe.setOnClick(() -> connection.sendCommand("subscribe Tic-tac-toe"));
        btnDisconnect.setOnClick(() -> {
            connection.disconnect();
            sceneManager.changeScene(new MainMenuScene(input, actions, sceneManager));
        });
    }

    @Override
    public void update(float dt) {
        mousePosition = new Vector2f(
                (Math.round(input.getMouseX() / 4) + 4f) * 4,
                (Math.round(input.getMouseY() / 4) + 16f) * 4);

        mousePoint.setTexture(input.isButtonDown(GLFW_MOUSE_BUTTON_LEFT) ? mouseGrabTexture : mousePointTexture);
        mousePoint.setPosition(mousePosition);

        btnGetPlayers.update(input);
        btnGetGames.update(input);
        btnSubscribe.update(input);
        btnDisconnect.update(input);
        background.update(dt, false, mousePosition.x, mousePosition.y);

        // Process network messages on main thread
        connection.update(msg -> {
            if (msg.equals("OK")) {
                // Command accepted
            } else if (msg.startsWith("ERR")) {
                serverStatus = "Error: " + msg.substring(4);
            } else if (msg.startsWith("SVR PLAYERLIST")) {
                playersList = "Players: " + msg.substring(15);
            } else if (msg.startsWith("SVR GAMELIST")) {
                gamesList = "Games: " + msg.substring(13);
            } else if (msg.startsWith("SVR GAME MATCH")) {
                serverStatus = "Match found!";
                sceneManager.changeScene(new TicTacToeScene(input, actions, sceneManager, connection));
            } else {
                serverStatus = "Status: " + msg;
            }
        });
    }

    @Override
    public void render() {
        glClear(GL_COLOR_BUFFER_BIT);
        background.render(currentWidth, currentHeight);

        textRenderer.drawText(gameFont, "MULTIPLAYER LOBBY", 50, 80, new Vector4f(1, 1, 1, 1));
        textRenderer.drawText(smallFont, serverStatus, 50, 140, new Vector4f(1, 1, 0, 1));

        textRenderer.drawText(smallFont, playersList, 50, 220, new Vector4f(0.8f, 0.8f, 1, 1));
        textRenderer.drawText(smallFont, gamesList, 50, 280, new Vector4f(0.8f, 1, 0.8f, 1));

        btnGetPlayers.render(spriteRenderer, textRenderer);
        btnGetGames.render(spriteRenderer, textRenderer);
        btnSubscribe.render(spriteRenderer, textRenderer);
        btnDisconnect.render(spriteRenderer, textRenderer);

        spriteRenderer.draw(mousePoint);
    }

    @Override
    public void resize(int width, int height) {
        this.currentWidth = width;
        this.currentHeight = height;
        spriteRenderer.setProjection(width, height);
        textRenderer.setProjection(width, height);
    }

    @Override
    public void cleanup() {
        background.cleanup();
        textRenderer.cleanup();
    }
}