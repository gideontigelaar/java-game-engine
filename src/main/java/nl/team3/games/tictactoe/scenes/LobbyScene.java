package nl.team3.games.tictactoe.scenes;

import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.SceneManager;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import nl.team3.engine.ui.Button;
import nl.team3.games.tictactoe.network.ServerConnection;
import nl.team3.games.tictactoe.network.ServerMessage;
import org.joml.Vector4f;

import java.util.List;

public class LobbyScene extends BaseScene {
    private static final String GAME_TYPE = "Tic-tac-toe";
    private static final String TITLE = "MULTIPLAYER LOBBY";
    private static final int MAX_LISTED = 5;
    private static final Vector4f TITLE_COLOR = new Vector4f(1, 1, 1, 1);
    private static final Vector4f STATUS_COLOR = new Vector4f(1, 1, 0, 1);
    private static final Vector4f PLAYERS_COLOR = new Vector4f(0.8f, 0.8f, 1, 1);
    private static final Vector4f GAMES_COLOR = new Vector4f(0.8f, 1, 0.8f, 1);

    private final String playerName;
    private final boolean searchOnStart;

    private Button btnFindMatch;
    private Button btnGetPlayers;
    private Button btnGetGames;
    private Button btnDisconnect;

    private String serverStatus;
    private String playersList = "Players: -";
    private String gamesList = "Games: -";
    private boolean searching;
    private String challengeNumber;

    private float titleY;
    private float statusY;
    private float playersY;
    private float gamesY;

    public LobbyScene(InputManager input, ActionMap actions, SceneManager sceneManager, ServerConnection connection,
                      String playerName, boolean searchOnStart) {
        super(input, actions, sceneManager, connection);
        this.playerName = playerName;
        this.searchOnStart = searchOnStart;
    }

    @Override
    protected void onInit(AssetManager assets) {
        btnFindMatch = createButton("FIND MATCH", BUTTON_WIDTH, this::findMatch);
        btnGetPlayers = createButton("GET PLAYERS", BUTTON_WIDTH, () -> connection.sendCommand("get playerlist"));
        btnGetGames = createButton("GET GAMES", BUTTON_WIDTH, () -> connection.sendCommand("get gamelist"));
        btnDisconnect = createButton("DISCONNECT", BUTTON_WIDTH, this::disconnect);

        serverStatus = "Logged in as " + playerName;
        connection.sendCommand("get playerlist");
        connection.sendCommand("get gamelist");
        if (searchOnStart) {
            findMatch();
        }
    }

    private void findMatch() {
        if (challengeNumber != null) {
            connection.sendCommand("challenge accept " + challengeNumber);
            challengeNumber = null;
            serverStatus = "Starting match...";
        } else if (!searching) {
            connection.sendCommand("subscribe " + GAME_TYPE);
            searching = true;
            serverStatus = "Searching for an opponent...";
        }
        updateFindMatchText();
    }

    private void updateFindMatchText() {
        if (challengeNumber != null) {
            btnFindMatch.setText("ACCEPT CHALLENGE");
        } else {
            btnFindMatch.setText(searching ? "SEARCHING..." : "FIND MATCH");
        }
    }

    private void disconnect() {
        connection.disconnect();
        transitionTo(new MainMenuScene(input, actions, sceneManager), btnFindMatch, btnGetPlayers, btnGetGames, btnDisconnect);
    }

    @Override
    protected void onUpdate(float dt) {
        if (!isTransitioning()) {
            btnFindMatch.update(input);
            btnGetPlayers.update(input);
            btnGetGames.update(input);
            btnDisconnect.update(input);
        }

        ServerMessage message;
        while (!isTransitioning() && (message = connection.poll()) != null) {
            handleMessage(message);
        }

        if (!isTransitioning() && !connection.isConnected()) {
            sceneManager.changeScene(new MainMenuScene(input, actions, sceneManager, "Connection lost!"));
        }
    }

    private void handleMessage(ServerMessage message) {
        switch (message.type()) {
            case "ERR" -> serverStatus = "Error: " + message.data();
            case "SVR PLAYERLIST" -> playersList = "Players: " + listText(message.items());
            case "SVR GAMELIST" -> gamesList = "Games: " + listText(message.items());
            case "SVR GAME MATCH" -> startMatch(message);
            case "SVR GAME CHALLENGE" -> receiveChallenge(message);
            case "SVR GAME CHALLENGE CANCELLED" -> cancelChallenge(message);
            default -> { }
        }
    }

    private static String listText(List<String> items) {
        if (items.size() <= MAX_LISTED) {
            return String.join(", ", items);
        }
        return String.join(", ", items.subList(0, MAX_LISTED)) + " +" + (items.size() - MAX_LISTED);
    }

    private void startMatch(ServerMessage match) {
        searching = false;
        if (!GAME_TYPE.equalsIgnoreCase(match.get("GAMETYPE"))) {
            connection.sendCommand("forfeit");
            serverStatus = "Can't play " + match.get("GAMETYPE");
            updateFindMatchText();
            return;
        }

        boolean playerStarts = match.get("PLAYERTOMOVE").equalsIgnoreCase(playerName);
        transitionTo(new OnlineMatchScene(input, actions, sceneManager, connection, playerName, match.get("OPPONENT"), playerStarts),
                btnFindMatch, btnGetPlayers, btnGetGames, btnDisconnect);
    }

    private void receiveChallenge(ServerMessage challenge) {
        if (!GAME_TYPE.equalsIgnoreCase(challenge.get("GAMETYPE"))) return;

        challengeNumber = challenge.get("CHALLENGENUMBER");
        serverStatus = challenge.get("CHALLENGER") + " challenges you!";
        updateFindMatchText();
    }

    private void cancelChallenge(ServerMessage cancelled) {
        if (!cancelled.get("CHALLENGENUMBER").equals(challengeNumber)) return;

        challengeNumber = null;
        serverStatus = "Challenge cancelled";
        updateFindMatchText();
    }

    @Override
    protected void onRender() {
        drawCenteredText(gameFont, TITLE, titleY, TITLE_COLOR);
        drawCenteredText(smallFont, serverStatus, statusY, STATUS_COLOR);
        drawCenteredText(smallFont, playersList, playersY, PLAYERS_COLOR);
        drawCenteredText(smallFont, gamesList, gamesY, GAMES_COLOR);

        btnFindMatch.render(spriteRenderer, textRenderer);
        btnGetPlayers.render(spriteRenderer, textRenderer);
        btnGetGames.render(spriteRenderer, textRenderer);
        btnDisconnect.render(spriteRenderer, textRenderer);
    }

    @Override
    protected void onResize(int width, int height) {
        float titleHeight = gameFont.getCapHeight();
        float lineHeight = smallFont.getCapHeight();

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

        btnFindMatch.setPosition(buttonX, y);
        y += BUTTON_HEIGHT + UI_SPACING;
        btnGetPlayers.setPosition(buttonX, y);
        y += BUTTON_HEIGHT + UI_SPACING;
        btnGetGames.setPosition(buttonX, y);
        y += BUTTON_HEIGHT + UI_SPACING;
        btnDisconnect.setPosition(buttonX, y);
    }
}