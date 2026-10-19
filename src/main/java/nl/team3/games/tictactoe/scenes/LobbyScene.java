package nl.team3.games.tictactoe.scenes;

import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.SceneManager;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import nl.team3.engine.ui.Button;
import nl.team3.games.tictactoe.network.ServerConnection;
import org.joml.Vector4f;

public class LobbyScene extends BaseScene {
    private static final String TITLE = "MULTIPLAYER LOBBY";
    private static final Vector4f TITLE_COLOR = new Vector4f(1, 1, 1, 1);
    private static final Vector4f STATUS_COLOR = new Vector4f(1, 1, 0, 1);
    private static final Vector4f PLAYERS_COLOR = new Vector4f(0.8f, 0.8f, 1, 1);
    private static final Vector4f GAMES_COLOR = new Vector4f(0.8f, 1, 0.8f, 1);

    private Button btnGetPlayers;
    private Button btnGetGames;
    private Button btnSubscribe;
    private Button btnDisconnect;

    private String serverStatus = "Connected. Awaiting actions...";
    private String playersList = "Players: []";
    private String gamesList = "Games: []";

    private float titleY;
    private float statusY;
    private float playersY;
    private float gamesY;

    public LobbyScene(InputManager input, ActionMap actions, SceneManager sceneManager, ServerConnection connection) {
        super(input, actions, sceneManager, connection);
    }

    @Override
    protected void onInit(AssetManager assets) {
        btnGetPlayers = createButton("GET PLAYERS", BUTTON_WIDTH, () -> connection.sendCommand("get playerlist"));
        btnGetGames = createButton("GET GAMES", BUTTON_WIDTH, () -> connection.sendCommand("get gamelist"));
        btnSubscribe = createButton("FIND MATCH", BUTTON_WIDTH, () -> connection.sendCommand("subscribe Tic-tac-toe"));
        // Back to the menu happens below once the connection is closed
        btnDisconnect = createButton("DISCONNECT", BUTTON_WIDTH, connection::disconnect);
    }

    @Override
    protected void onUpdate(float dt) {
        btnGetPlayers.update(input);
        btnGetGames.update(input);
        btnSubscribe.update(input);
        btnDisconnect.update(input);

        connection.update(msg -> {
            if (msg.startsWith("ERR")) {
                serverStatus = "Error: " + msg.substring(4);
            } else if (msg.startsWith("SVR PLAYERLIST")) {
                playersList = "Players: " + msg.substring(15);
            } else if (msg.startsWith("SVR GAMELIST")) {
                gamesList = "Games: " + msg.substring(13);
            } else if (msg.startsWith("SVR GAME MATCH")) {
                serverStatus = "Match found!";
                sceneManager.changeScene(new MatchScene(input, actions, sceneManager, connection));
            } else if (msg.startsWith("SVR GAME CHALLENGE")) {
                serverStatus = "Received a challenge!";
            }
        });

        if (!connection.isConnected()) {
            sceneManager.changeScene(new MainMenuScene(input, actions, sceneManager));
        }
    }

    @Override
    protected void onRender() {
        drawCenteredText(gameFont, TITLE, titleY, TITLE_COLOR);
        drawCenteredText(smallFont, serverStatus, statusY, STATUS_COLOR);
        drawCenteredText(smallFont, playersList, playersY, PLAYERS_COLOR);
        drawCenteredText(smallFont, gamesList, gamesY, GAMES_COLOR);

        btnGetPlayers.render(spriteRenderer, textRenderer);
        btnGetGames.render(spriteRenderer, textRenderer);
        btnSubscribe.render(spriteRenderer, textRenderer);
        btnDisconnect.render(spriteRenderer, textRenderer);
    }

    @Override
    protected void onResize(int width, int height) {
        float titleHeight = gameFont.getCapHeight();
        float lineHeight = smallFont.getCapHeight();

        // Title, three lines of server info and the buttons in one centered column
        float contentHeight = titleHeight + 2 * UI_SPACING + 3 * lineHeight + 2 * UI_SPACING
                + 2 * UI_SPACING + 4 * BUTTON_HEIGHT + 3 * UI_SPACING;
        float buttonX = Math.round((width - BUTTON_WIDTH) / 2f);
        float y = Math.max(0, Math.round((height - contentHeight) / 2f));

        titleY = y;
        y += titleHeight + 2 * UI_SPACING;
        statusY = y;
        y += lineHeight + UI_SPACING;
        playersY = y;
        y += lineHeight + UI_SPACING;
        gamesY = y;
        y += lineHeight + 2 * UI_SPACING;

        btnGetPlayers.setPosition(buttonX, y);
        y += BUTTON_HEIGHT + UI_SPACING;
        btnGetGames.setPosition(buttonX, y);
        y += BUTTON_HEIGHT + UI_SPACING;
        btnSubscribe.setPosition(buttonX, y);
        y += BUTTON_HEIGHT + UI_SPACING;
        btnDisconnect.setPosition(buttonX, y);
    }
}