package nl.team3.games.tictactoe;

import nl.team3.engine.graphics.*;
import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.Scene;
import nl.team3.engine.graphics.animation.*;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import org.joml.Vector2f;
import org.joml.Vector4f;
import nl.team3.engine.core.Config;
import nl.team3.engine.graphics.Sprite;
import nl.team3.engine.graphics.SpriteRenderer;
import nl.team3.engine.graphics.Texture;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_2;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

public class TicTacToeScene implements Scene {
    private ShaderProgram spriteShader;
    private SpriteRenderer spriteRenderer;
    private int currentHeight = Config.WINDOW_HEIGHT;
    private int currentWidth = Config.WINDOW_WIDTH;

    //mouse
    private boolean blue = true;
    private boolean lastblue = true;
    private Animation mouseGrabAnim;
    private Animation mouseReleaseAnim;
    private Texture mousePointTexture;
    private Texture mouseGrabTexture;
    private Sprite mousePoint;
    private Sprite mouseGrab;
    private Vector2f mousePosition;

    //board
    private Vector2f screenCenter = new Vector2f(Math.round((float) currentWidth/2),Math.round((float) currentHeight/2));
    private Sprite board;
    private Texture boardTexture;

    //pieceTest
    private Sprite Xpiece;
    private Texture XpieceTexture;
    private BackgroundRenderer background;
    private Vector2f mousePosEffect;
    private final InputManager input;
    private final ActionMap actions;

    private TextRenderer textRenderer;
    private Font gameFont;

    public TicTacToeScene(InputManager input, ActionMap actions) {
        this.input = input;
        this.actions = actions;
    }

    @Override
    public void init(AssetManager assets) {
        System.out.println("TicTacToeScene loaded");

        gameFont = assets.loadFont("gameFont", "/fonts/3x5-Microfont-Mono.ttf", 48f);
        textRenderer = new TextRenderer(assets);

        //initialize mouse
        mousePointTexture = assets.loadTexture("mousePoint", "/textures/tictactoe/cursorPoint.png");
        mouseGrabTexture = assets.loadTexture("mouseGrab", "/textures/tictactoe/cursorGrab.png");
        spriteRenderer = new SpriteRenderer(assets);
        input.setCursorVisible(false);
        mousePoint = new Sprite(mousePointTexture);
        mousePoint.setScale(4f);
        mousePoint.setAlpha(1f);

        //init Mouseanims
        mouseGrabAnim = Animation.builder()
                .sprite(mousePoint)
                .scale(new Vector2f(8f,8f), new Vector2f(6f,6f))
                .duration(0.3f)
                .easing("ExponentialOut")
                .build();
        mouseReleaseAnim = Animation.builder()
                .sprite(mousePoint)
                .scale(new Vector2f(6f,6f), new Vector2f(8f,8f))
                .duration(0.3f)
                .easing("ExponentialOut")
                .build();

        //board init
        XpieceTexture = assets.loadTexture("Xpiece", "/textures/tictactoe/board.png");
        Xpiece = new Sprite(XpieceTexture);
        Xpiece.setPosition(screenCenter.get(0), screenCenter.get(1) - 8f);
        Xpiece.setScale(8f);

        //init background
        background = new BackgroundRenderer(assets);
        mousePosEffect = new Vector2f(0f,0f);
    }

    @Override
    public void update(float dt) {
        //mouse logic
        mousePosition = new Vector2f(
                (Math.round(input.getMouseX()/4) + 4f)*4,
                (Math.round(input.getMouseY()/4) + 16f)*4);
        if(input.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT)){
            mouseGrabAnim.startAnimation();
        }
        if(input.isButtonReleased(GLFW_MOUSE_BUTTON_LEFT)){
            mouseReleaseAnim.startAnimation();
        }
        if(input.isButtonDown(GLFW_MOUSE_BUTTON_LEFT)){
            mousePoint.setTexture(mouseGrabTexture);
            mouseGrabAnim.UpdateAnimation(dt);
            mouseGrabAnim.setScale();
        }
        else{
            mousePoint.setTexture(mousePointTexture);
            mouseReleaseAnim.UpdateAnimation(dt);
            mouseReleaseAnim.setScale();
        }
        mousePoint.setPosition(mousePosition);

        //animation test
        if(input.isKeyPressed(GLFW_KEY_2)){
            blue = !blue;
            mousePosEffect = mousePosition;
            background.startAnimation();
        }

        //board rendering
        screenCenter = new Vector2f(Math.round((float) currentWidth/2),Math.round((float) currentHeight/2));
        background.update(dt, blue, (float) mousePosEffect.x, (float) mousePosEffect.y);
    }

    @Override
    public void render() {
        glClear(GL_COLOR_BUFFER_BIT);

        background.render(currentWidth,currentHeight);
        spriteRenderer.draw(Xpiece);
        textRenderer.drawText(gameFont, "TIC TAC TOE", 20, 60, new Vector4f(1, 1, 1, 1));
        textRenderer.drawText(gameFont, "Press '2' to toggle background", 20, 110, new Vector4f(0.8f, 0.8f, 0.8f, 1));
        spriteRenderer.draw(mousePoint);
    }

    @Override
    public void resize(int width, int height) {
        this.currentWidth = width;
        this.currentHeight = height;
        spriteRenderer.setProjection(width, height);
        textRenderer.setProjection(width, height);
        Xpiece.setPosition(new Vector2f(Math.round((float) width/2),Math.round((float) height/2)));
    }

    @Override
    public void cleanup() {
        System.out.println("TicTacToeScene closed");
        background.cleanup();
        textRenderer.cleanup();
    }
}