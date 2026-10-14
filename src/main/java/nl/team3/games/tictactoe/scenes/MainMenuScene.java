package nl.team3.games.tictactoe.scenes;

import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.SceneManager;
import nl.team3.engine.graphics.NineSlice;
import nl.team3.engine.graphics.Sprite;
import nl.team3.engine.graphics.Texture;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import nl.team3.engine.ui.Button;
import nl.team3.engine.ui.TextField;
import nl.team3.games.tictactoe.network.ServerConnection;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class MainMenuScene extends BaseScene {
    private TextField usernameInput;
    private Button btnLoginOnline;
    private Button btnPlayOffline;
    private Button btnQuit;

    private final String SERVER_HOST = "127.0.0.1";
    private final int SERVER_PORT = 7789;
    private String statusMessage = "";

    private NineSlice buttonSlice = new NineSlice(5, 6,5,5);

    private Texture btnTexture;
    private Texture btnDownTexture;
    private Texture anonUserProfile;

    private Sprite User;
    private Sprite btn1;
    private Sprite btn2;
    private Sprite btn3;
    private int heightMod1;
    private int heightMod2;
    private int heightMod3;

    private int windowWidth;
    private int windowHeight;
    public MainMenuScene(InputManager input, ActionMap actions, SceneManager sceneManager) {
        super(input, actions, sceneManager, null);
    }

    @Override
    protected void onInit(AssetManager assets) {
        System.out.println("MainMenuScene loaded");
        btnTexture = assets.loadTexture("btnTexture", "/textures/tictactoe/button.png");
        btnDownTexture = assets.loadTexture("btnPressedTexture", "/textures/tictactoe/buttonPressed.png");
        anonUserProfile = assets.loadTexture("anonUserProfile", "/textures/tictactoe/anonUserProfile.png");
        btn1 = new Sprite(btnTexture);
        btn2 = new Sprite(btnTexture);
        btn3 = new Sprite(btnTexture);
        User = new Sprite(anonUserProfile);
        User.setScale(4f);

        usernameInput = new TextField("player1", smallFont, new Vector2f(0, 0), new Vector2f(300, 50));

        btnLoginOnline = new Button("LOGIN ONLINE", smallFont, new Vector2f(0, 0), new Vector2f(300, 50));
        btnPlayOffline = new Button("PLAY OFFLINE", smallFont, new Vector2f(0, 0), new Vector2f(300, 50));
        btnQuit = new Button("QUIT GAME", smallFont, new Vector2f(0, 0), new Vector2f(300, 50));

        btnLoginOnline.setOnClick(this::attemptLogin);
        btnPlayOffline.setOnClick(() -> {
            sceneManager.changeScene(new MatchScene(input, actions, sceneManager, null));
        });
        btnQuit.setOnClick(input::requestClose);

    }

    private void attemptLogin() {
        try {
            ServerConnection newConnection = new ServerConnection(SERVER_HOST, SERVER_PORT);
            newConnection.connect();
            newConnection.sendCommand("login " + usernameInput.getText().toLowerCase());
            sceneManager.changeScene(new LobbyScene(input, actions, sceneManager, newConnection));
        } catch (Exception e) {
            statusMessage = "Connection failed!";
            System.out.println("Failed to connect to server: " + e.getMessage());
        }
    }

    @Override
    protected void onUpdate(float dt) {
        usernameInput.update(input);
        if (usernameInput.isSubmitPressed()) {
            attemptLogin();
        }
        btnLoginOnline.update(input);
        btnPlayOffline.update(input);
        btnQuit.update(input);

        if(btnLoginOnline.isPressed()){ btn1.setTexture(btnDownTexture); heightMod1 = 4; } else{ btn1.setTexture(btnTexture); heightMod1 = 0; };
        if(btnPlayOffline.isPressed()){ btn2.setTexture(btnDownTexture); heightMod2 = 4;} else{ btn2.setTexture(btnTexture); heightMod2 = 0;};
        if(btnQuit.isPressed()){ btn3.setTexture(btnDownTexture); heightMod3 = 4;} else{ btn3.setTexture(btnTexture); heightMod3 = 0;};
        onResize(windowWidth, windowHeight);
    }

    @Override
    protected void onRender() {

        spriteRenderer.draw(User);

        spriteRenderer.drawNineSlice(btn1,(windowWidth) / 2f,windowHeight * 0.40f + 110f,300,68,buttonSlice, 4);

        spriteRenderer.drawNineSlice(btn2,(windowWidth) / 2f,windowHeight * 0.40f + 110f + 80f,300,68,buttonSlice, 4);

        spriteRenderer.drawNineSlice(btn3,(windowWidth) / 2f,windowHeight * 0.40f + 110f + 160f,200,68,buttonSlice, 4);

        float titleWidth = gameFont.getTextWidth("TIC TAC TOE");
        textRenderer.drawText(gameFont, "TIC TAC TOE", (currentWidth - titleWidth) / 2f, currentHeight * 0.15f , new Vector4f(1, 1, 1, 1));

        //float labelWidth = smallFont.getTextWidth("Username:");
        //textRenderer.drawText(smallFont, "Username:", (currentWidth - labelWidth) / 2f, usernameInput.getPosition().y - 15, new Vector4f(0.8f, 0.8f, 0.8f, 1));

        if (!statusMessage.isEmpty()) {
            float statusWidth = smallFont.getTextWidth(statusMessage);
            textRenderer.drawText(smallFont, statusMessage, (currentWidth - statusWidth) / 2f, currentHeight * 0.7f, new Vector4f(1, 0, 0, 1));
        }



        usernameInput.render(spriteRenderer, textRenderer);
        btnLoginOnline.render(spriteRenderer, textRenderer);
        btnPlayOffline.render(spriteRenderer, textRenderer);
        btnQuit.render(spriteRenderer, textRenderer);
    }

    @Override
    protected void onResize(int width, int height) {
        windowWidth = width;
        windowHeight = height;
        float centerX = (width - 300f) / 2f;
        usernameInput.setPosition(new Vector2f(centerX, height * 0.40f));
        btnLoginOnline.setPosition(new Vector2f(centerX, height * 0.40f + 80f  + heightMod1));
        btnPlayOffline.setPosition(new Vector2f(centerX, height * 0.40f + 160f  + heightMod2));
        btnQuit.setPosition(new Vector2f(centerX, height * 0.40f + 240f + heightMod3));
        User.setPosition(new Vector2f(width / 2, height * 0.40f - 60f));
    }
}