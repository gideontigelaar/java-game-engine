package nl.team3.games.tictactoe.scenes;

import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.SceneManager;
import nl.team3.engine.graphics.Sprite;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import nl.team3.engine.ui.Button;
import nl.team3.engine.ui.TextField;
import nl.team3.games.tictactoe.BackgroundRenderer;
import nl.team3.games.tictactoe.network.ServerConnection;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.io.IOException;

public class MainMenuScene extends BaseScene {
    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 7789;
    private static final float NAME_FIELD_HEIGHT = 44f;
    private static final float STATUS_TIME = 3f;
    private static final Vector4f STATUS_COLOR = new Vector4f(1, 0, 0, 1);

    private TextField usernameInput;
    private Button btnLoginOnline;
    private Button btnPlayOffline;
    private Button btnQuit;

    private Sprite logo;
    private Sprite profile;

    private String statusMessage = "";
    private float statusTimer;
    private float statusY;

    public MainMenuScene(InputManager input, ActionMap actions, SceneManager sceneManager) {
        super(input, actions, sceneManager, null);
    }

    @Override
    protected void onInit(AssetManager assets) {
        System.out.println("MainMenuScene loaded");

        logo = createLogo();
        profile = new Sprite(assets.loadTexture("anonUserProfile", "/textures/tictactoe/anonUserProfile.png"));
        profile.setScale(UI_SCALE);

        usernameInput = new TextField("player1", smallFont, new Vector2f(), new Vector2f(BUTTON_WIDTH, NAME_FIELD_HEIGHT));
        usernameInput.setCentered(true);

        btnLoginOnline = createButton("LOGIN ONLINE", BUTTON_WIDTH, this::attemptLogin);
        btnPlayOffline = createButton("PLAY OFFLINE", BUTTON_WIDTH, this::playOffline);
        btnQuit = createButton("QUIT GAME", SMALL_BUTTON_WIDTH, input::requestClose);
    }

    private void attemptLogin() {
        ServerConnection newConnection = new ServerConnection(SERVER_HOST, SERVER_PORT);
        try {
            newConnection.connect();
        } catch (IOException e) {
            System.out.println("Failed to connect to server: " + e.getMessage());
            statusMessage = "Connection failed!";
            statusTimer = STATUS_TIME;
            return;
        }

        newConnection.sendCommand("login " + usernameInput.getText().toLowerCase());
        sceneManager.changeScene(new LobbyScene(input, actions, sceneManager, newConnection));
    }

    private void playOffline() {
        transitionTo(() -> new MatchScene(input, actions, sceneManager, null), BackgroundRenderer.PALETTE_GREEN,
                btnLoginOnline, btnPlayOffline, btnQuit);
    }

    @Override
    protected void onUpdate(float dt) {
        // Status text disappears again after a few seconds
        statusTimer = Math.max(0f, statusTimer - dt);

        if (isTransitioning()) {
            return;
        }

        usernameInput.update(input);
        if (usernameInput.isSubmitPressed()) {
            attemptLogin();
            return;
        }
        btnLoginOnline.update(input);
        btnPlayOffline.update(input);
        btnQuit.update(input);
    }

    @Override
    protected void onRender() {
        spriteRenderer.draw(logo);
        spriteRenderer.draw(profile);

        usernameInput.render(spriteRenderer, textRenderer);
        btnLoginOnline.render(spriteRenderer, textRenderer);
        btnPlayOffline.render(spriteRenderer, textRenderer);
        btnQuit.render(spriteRenderer, textRenderer);

        if (statusTimer > 0f) {
            drawCenteredText(smallFont, statusMessage, statusY, STATUS_COLOR);
        }
    }

    @Override
    protected void onResize(int width, int height) {
        float logoHeight = logo.getTexture().getHeight() * UI_SCALE;
        float profileHeight = profile.getTexture().getHeight() * UI_SCALE;
        float lineHeight = smallFont.getCapHeight();

        // One centered column, same spacing between everything below the logo, status line last
        float columnHeight = logoHeight + profileHeight + UI_SPACING + lineHeight
                + UI_SPACING + 3 * BUTTON_HEIGHT + 3 * UI_SPACING + lineHeight;
        // Gap under the logo shrinks on the smallest windows so the column still fits
        float logoGap = Math.max(UI_SPACING, Math.min(2 * UI_SPACING, height - columnHeight - 2 * UI_SPACING));
        float centerX = Math.round(width / 2f);
        float y = Math.max(0, Math.round((height - columnHeight - logoGap) / 2f));

        logo.setPosition(centerX, y + logoHeight / 2f);
        y += logoHeight + logoGap;

        profile.setPosition(centerX, y + profileHeight / 2f);
        y += profileHeight + UI_SPACING;

        // Field is taller than the name itself so it's easier to click
        usernameInput.setPosition(centerX - BUTTON_WIDTH / 2f, y + (lineHeight - NAME_FIELD_HEIGHT) / 2f);
        y += lineHeight + UI_SPACING;

        btnLoginOnline.setPosition(centerX - BUTTON_WIDTH / 2f, y);
        y += BUTTON_HEIGHT + UI_SPACING;
        btnPlayOffline.setPosition(centerX - BUTTON_WIDTH / 2f, y);
        y += BUTTON_HEIGHT + UI_SPACING;
        btnQuit.setPosition(centerX - SMALL_BUTTON_WIDTH / 2f, y);
        y += BUTTON_HEIGHT + UI_SPACING;

        statusY = y;
    }
}