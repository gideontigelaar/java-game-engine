package nl.team3.games.tictactoe;

import nl.team3.engine.graphics.*;
import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.Scene;
import nl.team3.engine.core.SceneManager;
import nl.team3.engine.graphics.animation.*;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import nl.team3.engine.ui.Button;
import nl.team3.games.tictactoe.network.ServerConnection;
import org.joml.Vector2f;
import org.joml.Vector4f;
import nl.team3.engine.core.Config;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_2;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

public class TicTacToeScene implements Scene {
    private SpriteRenderer spriteRenderer;
    private TextRenderer textRenderer;
    private BackgroundRenderer background;

    private int currentHeight = Config.WINDOW_HEIGHT;
    private int currentWidth = Config.WINDOW_WIDTH;
    private boolean blue = true;

    private Animation mouseGrabAnim;
    private Animation mouseReleaseAnim;
    private Texture mousePointTexture;
    private Texture mouseGrabTexture;
    private Sprite mousePoint;
    private Vector2f mousePosition;

    private Vector2f screenCenter = new Vector2f(Math.round((float) currentWidth/2), Math.round((float) currentHeight/2));
    private Sprite Xpiece;
    private Texture XpieceTexture;
    private Vector2f mousePosEffect;

    private Font gameFont;
    private Font smallFont;

    private Button btnAction;
    private boolean matchEnded = false;
    private String gameStatusMessage = "";

    private final InputManager input;
    private final ActionMap actions;
    private final SceneManager sceneManager;
    private final ServerConnection connection;

    public TicTacToeScene(InputManager input, ActionMap actions, SceneManager sceneManager, ServerConnection connection) {
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
        mousePoint.setAlpha(1f);

        mouseGrabAnim = Animation.builder().sprite(mousePoint).scale(new Vector2f(8f,8f), new Vector2f(6f,6f)).duration(0.3f).easing("ExponentialOut").build();
        mouseReleaseAnim = Animation.builder().sprite(mousePoint).scale(new Vector2f(6f,6f), new Vector2f(8f,8f)).duration(0.3f).easing("ExponentialOut").build();

        XpieceTexture = assets.loadTexture("Xpiece", "/textures/tictactoe/board.png");
        Xpiece = new Sprite(XpieceTexture);
        Xpiece.setPosition(screenCenter.get(0), screenCenter.get(1) - 8f);
        Xpiece.setScale(8f);

        mousePosEffect = new Vector2f(0f,0f);

        btnAction = new Button(connection != null ? "FORFEIT" : "LEAVE GAME", smallFont, new Vector2f(0, 0), new Vector2f(200, 50));
        btnAction.setOnClick(this::handleGameAction);

        if (connection != null) gameStatusMessage = "Match Started!";

        resize(currentWidth, currentHeight);
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
    public void update(float dt) {
        mousePosition = new Vector2f((float) input.getMouseX(), (float) input.getMouseY());

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

        if (input.isKeyPressed(GLFW_KEY_2)) {
            blue = !blue;
            mousePosEffect = mousePosition;
            background.startAnimation();
        }

        screenCenter = new Vector2f(Math.round((float) currentWidth/2), Math.round((float) currentHeight/2));
        background.update(dt, blue, (float) mousePosEffect.x, (float) mousePosEffect.y);

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
    public void render() {
        glClear(GL_COLOR_BUFFER_BIT);
        background.render(currentWidth, currentHeight);
        spriteRenderer.draw(Xpiece);

        String mode = connection != null ? "ONLINE" : "OFFLINE";
        float modeW = gameFont.getTextWidth(mode);
        textRenderer.drawText(gameFont, mode, (currentWidth - modeW) / 2f, currentHeight * 0.1f, new Vector4f(1, 1, 1, 1));

        if (!gameStatusMessage.isEmpty()) {
            float textW = smallFont.getTextWidth(gameStatusMessage);
            textRenderer.drawText(smallFont, gameStatusMessage, (currentWidth - textW) / 2f, currentHeight * 0.15f, new Vector4f(1, 1, 0, 1));
        }

        btnAction.render(spriteRenderer, textRenderer);
        spriteRenderer.draw(mousePoint);
    }

    @Override
    public void resize(int width, int height) {
        this.currentWidth = width;
        this.currentHeight = height;
        spriteRenderer.setProjection(width, height);
        textRenderer.setProjection(width, height);
        Xpiece.setPosition(new Vector2f(Math.round((float) width/2), Math.round((float) height/2)));
        btnAction.setPosition(new Vector2f((width - 200f) / 2f, height - 100f));
    }
}