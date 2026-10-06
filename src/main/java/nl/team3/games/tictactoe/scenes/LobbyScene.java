package nl.team3.games.tictactoe.scenes;

import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.SceneManager;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import nl.team3.engine.ui.Button;
import nl.team3.games.tictactoe.network.ServerConnection;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class LobbyScene extends BaseScene {
    private Button btnGetPlayers;
    private Button btnGetGames;
    private Button btnSubscribe;
    private Button btnDisconnect;

    private String serverStatus = "Connected. Awaiting actions...";
    private String playersList = "Players: []";
    private String gamesList = "Games: []";

    public LobbyScene(InputManager input, ActionMap actions, SceneManager sceneManager, ServerConnection connection) {
        super(input, actions, sceneManager, connection);
    }

    @Override
    protected void onInit(AssetManager assets) {
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
    }

    @Override
    protected void onResize(int width, int height) {
        float btnWidth = 250f;
        float centerX = (width - btnWidth) / 2f;
        btnGetPlayers.setPosition(new Vector2f(centerX, height * 0.55f));
        btnGetGames.setPosition(new Vector2f(centerX, height * 0.55f + 70f));
        btnSubscribe.setPosition(new Vector2f(centerX, height * 0.55f + 140f));
        btnDisconnect.setPosition(new Vector2f(centerX, height * 0.55f + 210f));
    }
}