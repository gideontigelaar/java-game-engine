package nl.team3.games.tictactoe.scenes;

import nl.team3.engine.core.SceneManager;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import nl.team3.engine.ui.Button;
import nl.team3.engine.ui.TextField;
import nl.team3.games.tictactoe.Settings;
import nl.team3.games.tictactoe.network.ServerAddress;
import nl.team3.games.tictactoe.network.ServerConnection;
import nl.team3.games.tictactoe.network.ServerMessage;
import org.joml.Vector2f;

import java.util.Locale;
import java.util.function.IntPredicate;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_TAB;

public class MainMenuScene extends MenuScene {
    private static final float FIELD_WIDTH = 480f;
    private static final int NAME_LENGTH = 16;
    private static final int ADDRESS_LENGTH = 28;
    private static final float LOGIN_TIMEOUT = 5f;
    private static final String NAME_PATTERN = "[a-z0-9_-]+";

    private TextField nameField;
    private TextField serverField;
    private Button btnLoginOnline;
    private Button btnPlayOffline;

    private ServerConnection login;
    private ServerAddress loginAddress;
    private String loginName;
    private boolean loginSent;
    private float loginTime;

    public MainMenuScene(InputManager input, ActionMap actions, SceneManager sceneManager) {
        this(input, actions, sceneManager, "");
    }

    public MainMenuScene(InputManager input, ActionMap actions, SceneManager sceneManager, String status) {
        super(input, actions, sceneManager, status);
    }

    @Override
    protected void initMenu() {
        nameField = createField(playerName, "ENTER YOUR NAME", NAME_LENGTH,
                c -> isAsciiLetterOrDigit(c) || c == '_' || c == '-');
        serverField = createField(Settings.getServerAddress(), "SERVER ADDRESS", ADDRESS_LENGTH,
                c -> isAsciiLetterOrDigit(c) || c == '.' || c == ':' || c == '-');
        serverField.setColors(DIM_WHITE, HOVER_COLOR, HIGHLIGHT_COLOR);

        btnLoginOnline = addButton("LOGIN ONLINE", this::openOnline);
        btnPlayOffline = addButton("PLAY OFFLINE", () -> open(new OfflineMenuScene(input, actions, sceneManager)));
        addSmallButton("QUIT GAME", input::requestClose);
    }

    private TextField createField(String text, String placeholder, int maxLength, IntPredicate allowedChars) {
        TextField field = new TextField(text, smallFont, new Vector2f(), new Vector2f(FIELD_WIDTH, smallFont.getCapHeight() + LINE_GAP));
        field.setCentered(true);
        field.setPlaceholder(placeholder);
        field.setMaxLength(maxLength);
        field.setAllowedChars(allowedChars);
        return field;
    }

    private static boolean isAsciiLetterOrDigit(int c) {
        return c < 128 && Character.isLetterOrDigit(c);
    }

    private void openOnline() {
        String name = nameField.getText().trim().toLowerCase(Locale.ROOT);
        if (name.isEmpty()) {
            focus(nameField, "Enter your name first!");
        } else if (!name.matches(NAME_PATTERN)) {
            focus(nameField, "Use letters and numbers only!");
        } else {
            ServerAddress address = ServerAddress.parse(serverField.getText());
            if (address == null) {
                focus(serverField, "Enter a server address like " + Settings.DEFAULT_SERVER);
            } else {
                startLogin(name, address);
            }
        }
    }

    private void startLogin(String name, ServerAddress address) {
        loginName = name;
        loginAddress = address;
        loginSent = false;
        loginTime = 0f;
        login = new ServerConnection(address.host(), address.port());
        login.connect();
        setLoginButtonsEnabled(false);
        showProgress("Connecting to " + address + "...");
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
                open(new LobbyScene(input, actions, sceneManager, login, loginName, false));
                login = null;
                return;
            }
            if (message.is("ERR")) {
                stopLogin("Login failed: " + message.data());
                return;
            }
        }

        if (!login.isConnecting() && !login.isConnected()) {
            stopLogin("Can't reach " + loginAddress);
        } else if (loginTime > LOGIN_TIMEOUT) {
            stopLogin("No answer from the server!");
        }
    }

    private void stopLogin(String error) {
        login.disconnect();
        login = null;
        setLoginButtonsEnabled(true);
        showStatus(error);
    }

    private void setLoginButtonsEnabled(boolean enabled) {
        btnLoginOnline.setEnabled(enabled);
        btnPlayOffline.setEnabled(enabled);
    }

    private void focus(TextField field, String error) {
        nameField.setFocused(field == nameField);
        serverField.setFocused(field == serverField);
        showStatus(error);
    }

    @Override
    protected void updateMenu(float dt) {
        if (login != null) {
            updateLogin(dt);
            return;
        }

        nameField.update(input);
        serverField.update(input);

        if (input.isKeyPressed(GLFW_KEY_TAB)) {
            boolean toServer = nameField.isFocused();
            nameField.setFocused(!toServer);
            serverField.setFocused(toServer);
        }
        if (nameField.isSubmitPressed() || serverField.isSubmitPressed()) {
            openOnline();
        }
    }

    @Override
    protected void renderHeader() {
        nameField.render(spriteRenderer, textRenderer);
        serverField.render(spriteRenderer, textRenderer);
    }

    @Override
    protected void layoutHeader(float centerX) {
        float x = centerX - FIELD_WIDTH / 2f;
        nameField.setPosition(x, nameY - LINE_GAP / 2f);
        serverField.setPosition(x, detailY - LINE_GAP / 2f);
    }

    @Override
    public void cleanup() {
        Settings.save(nameField.getText(), serverField.getText());
    }
}