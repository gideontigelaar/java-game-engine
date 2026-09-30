package nl.team3.games.tictactoe;

import nl.team3.engine.graphics.*;
import nl.team3.engine.core.Scene;
import nl.team3.engine.core.Config;
import nl.team3.engine.graphics.animation.Animation;
import nl.team3.engine.graphics.animation.AnimationSequence;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import org.joml.Vector2f;


import java.util.List;

import static org.lwjgl.opengl.GL11C.*;

public class TicTacToeScene implements Scene {

    private ShaderProgram spriteShader;
    private SpriteRenderer spriteRenderer;
    private Texture testTexture;
    private Sprite testSprite;





    private Animation movetest = new Animation(
            new Vector2f(0, 200), //startpos
            new Vector2f(400, 200),  //endpos
            new Vector2f(0.5f,0.02f), //startscale
            new Vector2f(0.1f, 0.15f), //endscale
            0, 0, 0.6f, "ExponentialOut", testSprite); //startrot, endrot, duration

    private Animation movetest2 = new Animation(
            new Vector2f(400, 200), //startpos
            new Vector2f(390, 200),  //endpos
            new Vector2f(0.1f,0.15f), //startscale
            new Vector2f(0.1f, 0.1f), //endscale
            0, 0, 0.1f, "Linear", testSprite); //startrot, endrot, duration

    private AnimationSequence sequence = new AnimationSequence(List.of(movetest, movetest2));


    int x = 0;

    private final InputManager input;
    private final ActionMap actions;

    public TicTacToeScene(InputManager input, ActionMap actions) {
        this.input = input;
        this.actions = actions;
    }

    @Override
    public void init() {
        System.out.println("TicTacToeScene loaded");

        String vertexSource = ResourceLoader.readResource("/shaders/sprite.vert");
        String fragmentSource = ResourceLoader.readResource("/shaders/sprite.frag");
        spriteShader = new ShaderProgram(vertexSource, fragmentSource);

        testTexture = Texture.load("/images/testpng.png");

        spriteRenderer = new SpriteRenderer(spriteShader, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);

        testSprite = new Sprite(testTexture);
        testSprite.setPosition(200, 150);
        testSprite.setScale(0.1f);
        testSprite.setRotation(0f);
        testSprite.setAlpha(1f);

        movetest.SetSprite(testSprite);
        movetest2.SetSprite(testSprite);

        sequence.startSequence();
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void update(float dt) {
        sequence.Update(dt);
        if(actions.isActionDown("pause")){
            movetest.startAnimation();
        }
    }


    @Override
    public void render() {
        glClear(GL_COLOR_BUFFER_BIT);
        spriteRenderer.draw(testSprite);
    }

    @Override
    public void cleanup() {
        System.out.println("TicTacToeScene closed");
        testTexture.cleanup();
        spriteShader.cleanup();
    }

    @Override
    public void resize(int width, int height) {
        glViewport(0, 0, width, height);

    }
}