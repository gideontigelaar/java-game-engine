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

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

import java.util.List;

import static org.lwjgl.opengl.GL11C.*;

public class TicTacToeScene implements Scene {

    private ShaderProgram spriteShader;
    private SpriteRenderer spriteRenderer;
    private Texture testTexture;
    private Sprite testSprite;

    private Animation anim1;








    int x = 0;

    private final InputManager input;
    private final ActionMap actions;

    public TicTacToeScene(InputManager input, ActionMap actions) {
        this.input = input;
        this.actions = actions;
    }

    @Override
    public void init(AssetManager assets) {

        System.out.println("TicTacToeScene loaded");

        testTexture = assets.loadTexture("test", "/images/testpng.png");
        spriteRenderer = new SpriteRenderer(assets);

        testSprite = new Sprite(testTexture);
        testSprite.setPosition(200, 150);
        testSprite.setScale(0.1f);
        testSprite.setRotation(0f);
        testSprite.setAlpha(1f);
    }

    @Override
    public void update(float dt) {

    }


    @Override
    public void render() {
        glClear(GL_COLOR_BUFFER_BIT);
        spriteRenderer.draw(testSprite);
    }



    @Override
    public void cleanup() {
        System.out.println("TicTacToeScene closed");
    }

    @Override
    public void resize(int width, int height) {
        glViewport(0, 0, width, height);

    }
}