package nl.team3.games.tictactoe;

import nl.team3.engine.graphics.*;
import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.Scene;
import nl.team3.engine.graphics.animation.*;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import org.joml.Vector2f;

import nl.team3.engine.graphics.Sprite;
import nl.team3.engine.graphics.SpriteRenderer;
import nl.team3.engine.graphics.Texture;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

public class TicTacToeScene implements Scene {

    private ShaderProgram spriteShader;
    private SpriteRenderer spriteRenderer;

    //mouse

    private Animation mouseGrabAnim;
    private Animation mouseReleaseAnim;

    private Texture mousePointTexture;
    private Texture mouseGrabTexture;
    private Sprite mousePoint;
    private Sprite mouseGrab;
    private Vector2f mousePosition;











    private final InputManager input;
    private final ActionMap actions;
    private int currentHeight;
    private int currentWidth;

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
        mousePoint.setScale(3f);
        mousePoint.setAlpha(1f);

        //init anims


    }

    @Override
    public void update(float dt) {

        //mouse logic
        mousePosition = new Vector2f(
                (Math.round(input.getMouseX()/4) + 4f)*4,
                (Math.round(input.getMouseY()/4) + 16f)*4);
        if(input.isButtonDown(GLFW_MOUSE_BUTTON_LEFT)){
            mousePoint.setTexture(mouseGrabTexture);
        }
        else{ mousePoint.setTexture(mousePointTexture);}
        mousePoint.setPosition(mousePosition);

    }


    @Override
    public void render() {
        glClear(GL_COLOR_BUFFER_BIT);
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