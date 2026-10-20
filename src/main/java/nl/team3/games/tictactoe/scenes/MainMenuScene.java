package nl.team3.games.tictactoe.scenes;

import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.SceneManager;
import nl.team3.engine.graphics.Sprite;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import nl.team3.engine.ui.Button;
import nl.team3.engine.ui.TextField;
import nl.team3.games.tictactoe.network.ServerConnection;
import nl.team3.games.tictactoe.network.ServerMessage;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class MainMenuScene extends BaseScene {
    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 7789;
    private static final float NAME_FIELD_HEIGHT = 44f;
    private static final float STATUS_TIME = 3f;
    private static final float LOGIN_TIMEOUT = 5f;
    private static final String NAME_PATTERN = "[a-z0-9_-]+";
    private static final Vector4f STATUS_COLOR = new Vector4f(1, 0, 0, 1);
    private static final Vector4f CONNECTING_COLOR = new Vector4f(1, 1, 1, 1);

    private TextField usernameInput;
    private Button btnLoginOnline;
    private Button btnPlayOffline;
    private Button btnPlayAI;
    private Button btnQuit;

    private Sprite logo;
    private Sprite profile;

    private String statusMessage = "";
    private float statusTimer;
    private float statusY;

    private ServerConnection login;
    private String loginName;
    private boolean loginSent;
    private float loginTime;

    public MainMenuScene(InputManager input, ActionMap actions, SceneManager sceneManager) {
        this(input, actions, sceneManager, "");
    }

    public MainMenuScene(InputManager input, ActionMap actions, SceneManager sceneManager, String status) {
        super(input, actions, sceneManager, null);
        if (!status.isEmpty()) {
            showStatus(status);
        }
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
        btnPlayAI = createButton("PLAY VS AI", BUTTON_WIDTH, this::playAI);
        btnQuit = createButton("QUIT GAME", SMALL_BUTTON_WIDTH, input::requestClose);
    }

    private void attemptLogin() {
        String name = usernameInput.getText().trim().toLowerCase();
        if (!name.matches(NAME_PATTERN)) {
            showStatus("Use letters and numbers only!");
            return;
        }

        loginName = name;
        loginSent = false;
        loginTime = 0f;
        login = new ServerConnection(SERVER_HOST, SERVER_PORT);
        login.connect();
    }

    private void updateLogin(float dt) {
        loginTime += dt;
        if (login.isConnected() && !loginSent) {
            login.sendCommand("login " + loginName);
            loginSent = true;
        }

        ServerMessage message;
        while ((message = login.poll()) != null) {
            if (message.is("OK")) {
                transitionTo(new LobbyScene(input, actions, sceneManager, login, loginName, false), btnLoginOnline, btnPlayOffline, btnPlayAI, btnQuit);
                login = null;
                return;
            }
            if (message.is("ERR")) {
                login.disconnect();
                login = null;
                showStatus("Login failed: " + message.data());
                return;
            }
        }

        if (!login.isConnecting() && !login.isConnected()) {
            login = null;
            showStatus("Connection failed!");
        } else if (loginTime > LOGIN_TIMEOUT) {
            login.disconnect();
            login = null;
            showStatus("No answer from server!");
        }
    }

    private void showStatus(String message) {
        statusMessage = message;
        statusTimer = STATUS_TIME;
    }

    private void playOffline() {
        // Gewoon 2 spelers lokaal naast elkaar op 1 laptop
        transitionTo(new MatchScene(input, actions, sceneManager), btnLoginOnline, btnPlayOffline, btnPlayAI, btnQuit);
    }

    private void playAI() {
        // Schakelt over naar de AI-moeilijkheidsgraad selectie
        transitionTo(new AIDifficultyScene(input, actions, sceneManager), btnLoginOnline, btnPlayOffline, btnPlayAI, btnQuit);
    }

    @Override
    protected void onUpdate(float dt) {
        statusTimer = Math.max(0f, statusTimer - dt);

        if (isTransitioning()) {
            return;
        }
        if (login != null) {
            updateLogin(dt);
            return;
        }

        usernameInput.update(input);
        if (usernameInput.isSubmitPressed()) {
            attemptLogin();
            return;
        }
        btnLoginOnline.update(input);
        btnPlayOffline.update(input);
        btnPlayAI.update(input);
        btnQuit.update(input);
    }

    @Override
    protected void onRender() {
        spriteRenderer.draw(logo);
        spriteRenderer.draw(profile);

        usernameInput.render(spriteRenderer, textRenderer);
        btnLoginOnline.render(spriteRenderer, textRenderer);
        btnPlayOffline.render(spriteRenderer, textRenderer);
        btnPlayAI.render(spriteRenderer, textRenderer);
        btnQuit.render(spriteRenderer, textRenderer);

        if (login != null) {
            drawCenteredText(smallFont, "Connecting...", statusY, CONNECTING_COLOR);
        } else if (statusTimer > 0f) {
            drawCenteredText(smallFont, statusMessage, statusY, STATUS_COLOR);
        }
    }

    @Override
    protected void onResize(int width, int height) {
        float logoHeight = logo.getTexture().getHeight() * UI_SCALE;
        float profileHeight = profile.getTexture().getHeight() * UI_SCALE;
        float lineHeight = smallFont.getCapHeight();

        float columnHeight = logoHeight + profileHeight + UI_SPACING + lineHeight
                + UI_SPACING + 4 * BUTTON_HEIGHT + 4 * UI_SPACING + lineHeight;
        float logoGap = Math.max(UI_SPACING, Math.min(2 * UI_SPACING, height - columnHeight - 2 * UI_SPACING));
        float centerX = Math.round(width / 2f);
        float y = Math.max(0, Math.round((height - columnHeight - logoGap) / 2f));

        logo.setPosition(centerX, y + logoHeight / 2f);
        y += logoHeight + logoGap;

        profile.setPosition(centerX, y + profileHeight / 2f);
        y += profileHeight + UI_SPACING;

        usernameInput.setPosition(centerX - BUTTON_WIDTH / 2f, y + (lineHeight - NAME_FIELD_HEIGHT) / 2f);
        y += lineHeight + UI_SPACING;

        btnLoginOnline.setPosition(centerX - BUTTON_WIDTH / 2f, y);
        y += BUTTON_HEIGHT + UI_SPACING;
        btnPlayOffline.setPosition(centerX - BUTTON_WIDTH / 2f, y);
        y += BUTTON_HEIGHT + UI_SPACING;
        btnPlayAI.setPosition(centerX - BUTTON_WIDTH / 2f, y);
        y += BUTTON_HEIGHT + UI_SPACING;
        btnQuit.setPosition(centerX - SMALL_BUTTON_WIDTH / 2f, y);
        y += BUTTON_HEIGHT + UI_SPACING;

        statusY = y;
    }
}