package nl.team3.games.tictactoe.scenes;

import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.Config;
import nl.team3.engine.core.SceneManager;
import nl.team3.engine.graphics.NineSlice;
import nl.team3.engine.graphics.Sprite;
import nl.team3.engine.graphics.Texture;
import nl.team3.engine.graphics.animation.Animation;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import nl.team3.engine.ui.Button;
import nl.team3.engine.ui.TextField;
import nl.team3.games.tictactoe.network.ServerConnection;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

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
    private Texture tripleTlogo;
    private Sprite logo;
    private Sprite User;
    private Sprite btn1;
    private Sprite btn2;
    private Sprite btn3;
    private int heightMod1;
    private int heightMod2;
    private int heightMod3;
    //animations
    private Animation windowWiden;
    private boolean transitionToGame;
    private long window;
    private int windowWidth;
    private int windowHeight;
    private int oldwindowX;
    private int oldwindowY;
    private boolean hasChangedPalette = false;
    public MainMenuScene(InputManager input, ActionMap actions, SceneManager sceneManager, long window) {
        super(input, actions, sceneManager, null);
        this.window = window;
    }

    @Override
    protected void onInit(AssetManager assets) {
        windowWiden = new Animation(Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT, "ExponentialInOut", 1f);
        System.out.println("MainMenuScene loaded");
        btnTexture = assets.loadTexture("btnTexture", "/textures/tictactoe/button.png");
        btnDownTexture = assets.loadTexture("btnPressedTexture", "/textures/tictactoe/buttonPressed.png");
        anonUserProfile = assets.loadTexture("anonUserProfile", "/textures/tictactoe/anonUserProfile.png");
        tripleTlogo = assets.loadTexture("tripleTlogo", "/textures/tictactoe/tripleTlogo.png");
        btn1 = new Sprite(btnTexture);
        btn2 = new Sprite(btnTexture);
        btn3 = new Sprite(btnTexture);
        User = new Sprite(anonUserProfile);
        User.setScale(4f);
        logo = new Sprite(tripleTlogo);
        logo.setScale(4f);

        usernameInput = new TextField("player1", smallFont, new Vector2f(0, 0), new Vector2f(300, 50));

        btnLoginOnline = new Button("LOGIN ONLINE", smallFont, new Vector2f(0, 0), new Vector2f(300, 50));
        btnPlayOffline = new Button("PLAY OFFLINE", smallFont, new Vector2f(0, 0), new Vector2f(300, 50));
        btnQuit = new Button("QUIT GAME", smallFont, new Vector2f(0, 0), new Vector2f(300, 50));

        btnLoginOnline.setOnClick(this::attemptLogin);
        btnPlayOffline.setOnClick(() -> {
            windowWiden.startAnimation();
            transitionToGame = true;
            int[] x = new int[1], y = new int[1];
            GLFW.glfwGetWindowPos(window, x,y);
            oldwindowX = x[0];
            oldwindowY = y[0];
        });
        btnQuit.setOnClick(input::requestClose);

    }

    private void attemptLogin() {
        try {
            ServerConnection newConnection = new ServerConnection(SERVER_HOST, SERVER_PORT);
            newConnection.connect();
            newConnection.sendCommand("login " + usernameInput.getText().toLowerCase());
            sceneManager.changeScene(new LobbyScene(input, actions, sceneManager, newConnection, window));
        } catch (Exception e) {
            statusMessage = "Connection failed!";
            System.out.println("Failed to connect to server: " + e.getMessage());
        }
    }


    //handles transition animation to next scene
    private void transition(float dt){

        windowWiden.UpdateAnimation(dt);

        GLFW.glfwSetWindowPos(window,oldwindowX - (Math.round(windowWiden.getValue()) - 600)/2,oldwindowY);
        GLFW.glfwSetWindowSize(window, Math.round(windowWiden.getValue()), windowHeight);

        if(windowWiden.getProgress() > 0.2f){
            if(!hasChangedPalette){
                background = getBackground();
                background.setPalette(0,0, 1);
                hasChangedPalette = true;
            }

        }

        if(windowWiden.getProgress() > 0.7f){
            sceneManager.changeScene(new MatchScene(input, actions, sceneManager, null, window));
            GLFW.glfwSetWindowSize(window, 800, windowHeight);

        }

    }

    @Override
    protected void onUpdate(float dt) {
        if(transitionToGame){
            transition(dt);
        }

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

        spriteRenderer.draw(logo);

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
        logo.setPosition((currentWidth) / 2f, currentHeight * 0.15f );
    }
}