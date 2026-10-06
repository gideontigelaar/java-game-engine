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
    private BackgroundRenderer background;

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

    private final InputManager input;
    private final ActionMap actions;
    private final SceneManager sceneManager;
    private final ServerConnection connection;

    private String serverStatus = "Connected. Awaiting actions...";
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

        textRenderer = assets.getTextRenderer();
        spriteRenderer = assets.getSpriteRenderer();
        background = assets.getBackgroundRenderer();

        mousePointTexture = assets.loadTexture("mousePoint", "/textures/tictactoe/cursorPoint.png");
        mouseGrabTexture = assets.loadTexture("mouseGrab", "/textures/tictactoe/cursorGrab.png");
        input.setCursorVisible(false);
        mousePoint = new Sprite(mousePointTexture);
        mousePoint.setScale(4f);

        btnGetPlayers = new Button("GET PLAYERS", smallFont, new Vector2f(0, 0), new Vector2f(250, 50));
        btnGetGames = new Button("GET GAMES", smallFont, new Vector2f(0, 0), new Vector2f(250, 50));
        btnSubscribe = new Button("FIND MATCH", smallFont, new Vector2f(0, 0), new Vector2f(250, 50));
        btnDisconnect = new Button("DISCONNECT", smallFont, new Vector2f(0, 0), new Vector2f(250, 50));

        btnGetPlayers.setOnClick(() -> connection.sendCommand("get playerlist"));
        btnGetGames.setOnClick(() -> connection.sendCommand("get gamelist"));
        btnSubscribe.setOnClick(() -> connection.sendCommand("subscribe Tic-tac-toe"));
        btnDisconnect.setOnClick(() -> {
            connection.disconnect();
            sceneManager.changeScene(new MainMenuScene(input, actions, sceneManager));
        });

        resize(currentWidth, currentHeight);
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

        btnGetPlayers.update(input);
        btnGetGames.update(input);
        btnSubscribe.update(input);
        btnDisconnect.update(input);
        background.update(dt, false, mousePosition.x, mousePosition.y);

        connection.update(msg -> {
            if (msg.startsWith("ERR")) {
                serverStatus = "Error: " + msg.substring(4);
            } else if (msg.startsWith("SVR PLAYERLIST")) {
                playersList = "Players: " + msg.substring(15);
            } else if (msg.startsWith("SVR GAMELIST")) {
                gamesList = "Games: " + msg.substring(13);
            } else if (msg.startsWith("SVR GAME MATCH")) {
                serverStatus = "Match found!";
                sceneManager.changeScene(new TicTacToeScene(input, actions, sceneManager, connection));
            } else if (msg.startsWith("SVR GAME CHALLENGE")) {
                serverStatus = "Received a challenge!";
            }
        });

        if (!connection.isConnected()) {
            sceneManager.changeScene(new MainMenuScene(input, actions, sceneManager));
        }
    }

    @Override
    public void render() {
        glClear(GL_COLOR_BUFFER_BIT);
        background.render(currentWidth, currentHeight);

        float titleW = gameFont.getTextWidth("MULTIPLAYER LOBBY");
        textRenderer.drawText(gameFont, "MULTIPLAYER LOBBY", (currentWidth - titleW) / 2f, currentHeight * 0.10f, new Vector4f(1, 1, 1, 1));

        float statusW = smallFont.getTextWidth(serverStatus);
        textRenderer.drawText(smallFont, serverStatus, (currentWidth - statusW) / 2f, currentHeight * 0.20f, new Vector4f(1, 1, 0, 1));

        float playersW = smallFont.getTextWidth(playersList);
        textRenderer.drawText(smallFont, playersList, (currentWidth - playersW) / 2f, currentHeight * 0.30f, new Vector4f(0.8f, 0.8f, 1, 1));

        float gamesW = smallFont.getTextWidth(gamesList);
        textRenderer.drawText(smallFont, gamesList, (currentWidth - gamesW) / 2f, currentHeight * 0.40f, new Vector4f(0.8f, 1, 0.8f, 1));

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

        float btnWidth = 250f;
        float centerX = (width - btnWidth) / 2f;
        btnGetPlayers.setPosition(new Vector2f(centerX, height * 0.55f));
        btnGetGames.setPosition(new Vector2f(centerX, height * 0.55f + 70f));
        btnSubscribe.setPosition(new Vector2f(centerX, height * 0.55f + 140f));
        btnDisconnect.setPosition(new Vector2f(centerX, height * 0.55f + 210f));
    }
}