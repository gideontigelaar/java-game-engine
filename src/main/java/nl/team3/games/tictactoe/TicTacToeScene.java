package nl.team3.games.tictactoe;

import nl.team3.engine.graphics.*;
import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.Scene;
import nl.team3.engine.graphics.animation.*;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import org.joml.Vector2f;
import nl.team3.engine.core.Config;
import nl.team3.engine.graphics.Sprite;
import nl.team3.engine.graphics.SpriteRenderer;
import nl.team3.engine.graphics.Texture;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

public class TicTacToeScene implements Scene {

    private ShaderProgram spriteShader;
    private SpriteRenderer spriteRenderer;

    private int currentHeight = Config.WINDOW_HEIGHT;
    private int currentWidth = Config.WINDOW_WIDTH;

    //mouse

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








    private final InputManager input;
    private final ActionMap actions;


    public TicTacToeScene(InputManager input, ActionMap actions) {
        this.input = input;
        this.actions = actions;
    }

    @Override
    public void init(AssetManager assets) {
        System.out.println("TicTacToeScene loaded");

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
                .scale(new Vector2f(4f,4f), new Vector2f(3f,3f))
                .duration(0.3f)
                .easing("ExponentialOut")
                .build();
        mouseReleaseAnim = Animation.builder()
                .sprite(mousePoint)
                .scale(new Vector2f(3f,3f), new Vector2f(4f,4f))
                .duration(0.3f)
                .easing("ExponentialOut")
                .build();

        //init Board
        boardTexture = assets.loadTexture("board", "/textures/tictactoe/board.png");
        board = new Sprite(boardTexture);
        board.setPosition(screenCenter);
        board.setScale(8f);

        //init Test piece
        XpieceTexture = assets.loadTexture("Xpiece", "/textures/tictactoe/x.png");
        Xpiece = new Sprite(XpieceTexture);
        Xpiece.setPosition(screenCenter.get(0), screenCenter.get(1) - 8f);
        Xpiece.setScale(8f);
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

        //board rendering
        screenCenter = new Vector2f(Math.round((float) currentWidth/2),Math.round((float) currentHeight/2));







    }


    @Override
    public void render() {
        glClear(GL_COLOR_BUFFER_BIT);
        spriteRenderer.draw(board);
        spriteRenderer.draw(Xpiece);
        spriteRenderer.draw(mousePoint);

    }

    @Override
    public void resize(int width, int height) {
        // Store new dimensions every time window resizes
        this.currentWidth = width;
        this.currentHeight = height;

        spriteRenderer.setProjection(width, height);
    }

    @Override
    public void cleanup() {
        System.out.println("TicTacToeScene closed");
    }


}