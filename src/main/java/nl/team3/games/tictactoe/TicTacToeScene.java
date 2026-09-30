package nl.team3.games.tictactoe;

import nl.team3.engine.graphics.*;
import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.Scene;
import nl.team3.engine.core.Config;
import nl.team3.engine.graphics.animation.Animation;
import nl.team3.engine.graphics.animation.AnimationSequence;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import org.joml.Vector2f;

import nl.team3.engine.graphics.Sprite;
import nl.team3.engine.graphics.SpriteRenderer;
import nl.team3.engine.graphics.Texture;
import nl.team3.engine.graphics.animation.Animation;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

import java.util.List;

import static org.lwjgl.opengl.GL11C.*;

public class TicTacToeScene implements Scene {

    private ShaderProgram spriteShader;
    private SpriteRenderer spriteRenderer;
    private Texture mousePointTexture;
    private Texture mouseGrabTexture;
    private Sprite mousePoint;
    private Sprite mouseGrab;
    private Vector2f mousePosition;









    int x = 0;

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

        mousePointTexture = assets.loadTexture("mousePoint", "/textures/tictactoe/ase/cursorPoint.png");
        mouseGrabTexture = assets.loadTexture("mouseGrab", "/textures/tictactoe/ase/cursorGrab.png");
        spriteRenderer = new SpriteRenderer(assets);
        input.setCursorVisible(false);
        mousePoint = new Sprite(mousePointTexture);
        mousePoint.setScale(3f);
        mousePoint.setAlpha(1f);


    }

    @Override
    public void update(float dt) {
        mousePosition = new Vector2f(
                (Math.round(input.getMouseX()/3) + 3f)*3,
                (Math.round(input.getMouseY()/3) + 12f)*3);
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