package nl.team3.games.tictactoe.scenes;

import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.SceneManager;
import nl.team3.engine.graphics.Sprite;
import nl.team3.engine.graphics.Texture;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import nl.team3.engine.ui.Button;
import nl.team3.games.tictactoe.network.ServerConnection;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class MatchScene extends BaseScene {
    private Sprite Xpiece;
    private Texture XpieceTexture;

    private Button btnAction;
    private boolean matchEnded = false;
    private String gameStatusMessage = "";

    public MatchScene(InputManager input, ActionMap actions, SceneManager sceneManager, ServerConnection connection) {
        super(input, actions, sceneManager, connection);
    }

    @Override
    protected void onInit(AssetManager assets) {
        System.out.println("Game loaded");

        XpieceTexture = assets.loadTexture("Xpiece", "/textures/tictactoe/board.png");
        Xpiece = new Sprite(XpieceTexture);
        Xpiece.setScale(8f);

        btnAction = new Button(connection != null ? "FORFEIT" : "LEAVE GAME", smallFont, new Vector2f(0, 0), new Vector2f(200, 50));
        btnAction.setOnClick(this::handleGameAction);

        if (connection != null) gameStatusMessage = "Match Started!";
    }

    private void handleGameAction() {
        if (connection != null && !matchEnded) {
            connection.sendCommand("forfeit");
        }

        if (connection != null && connection.isConnected()) {
            sceneManager.changeScene(new LobbyScene(input, actions, sceneManager, connection));
        } else {
            sceneManager.changeScene(new MainMenuScene(input, actions, sceneManager));
        }
    }

    @Override
    protected void onUpdate(float dt) {
        btnAction.update(input);

        if (connection != null) {
            connection.update(msg -> {
                if (msg.startsWith("SVR GAME WIN")) {
                    gameStatusMessage = "YOU WIN! " + extractComment(msg);
                    endMatch();
                } else if (msg.startsWith("SVR GAME LOSS")) {
                    gameStatusMessage = "YOU LOSE! " + extractComment(msg);
                    endMatch();
                } else if (msg.startsWith("SVR GAME DRAW")) {
                    gameStatusMessage = "DRAW! " + extractComment(msg);
                    endMatch();
                } else if (msg.startsWith("SVR GAME YOURTURN")) {
                    gameStatusMessage = "Your turn!";
                }
            });

            if (!connection.isConnected()) {
                sceneManager.changeScene(new MainMenuScene(input, actions, sceneManager));
            }
        }
    }

    private void endMatch() {
        matchEnded = true;
        btnAction.setText("BACK TO LOBBY");
        btnAction.setColors(new Vector4f(0, 1, 0, 1), new Vector4f(0.8f, 1, 0.8f, 1), new Vector4f(0, 0.5f, 0, 1));
    }

    private String extractComment(String msg) {
        int idx = msg.indexOf("COMMENT: \"");
        if (idx != -1) {
            int endIdx = msg.indexOf("\"", idx + 10);
            if (endIdx != -1) {
                return msg.substring(idx + 10, endIdx);
            }
        }
        return "";
    }

    @Override
    protected void onRender() {
        spriteRenderer.draw(Xpiece);

        String mode = connection != null ? "ONLINE" : "OFFLINE";
        float modeW = gameFont.getTextWidth(mode);
        textRenderer.drawText(gameFont, mode, (currentWidth - modeW) / 2f, currentHeight * 0.1f, new Vector4f(1, 1, 1, 1));

        if (!gameStatusMessage.isEmpty()) {
            float textW = smallFont.getTextWidth(gameStatusMessage);
            textRenderer.drawText(smallFont, gameStatusMessage, (currentWidth - textW) / 2f, currentHeight * 0.15f, new Vector4f(1, 1, 0, 1));
        }

        btnAction.render(spriteRenderer, textRenderer);
    }

    @Override
    protected void onResize(int width, int height) {
        Xpiece.setPosition(new Vector2f(Math.round((float) width/2), Math.round((float) height/2)));
        btnAction.setPosition(new Vector2f((width - 200f) / 2f, height - 100f));
    }
}