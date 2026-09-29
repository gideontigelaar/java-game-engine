package nl.team3.games.tictactoe;

import nl.team3.engine.core.Config;
import nl.team3.engine.core.Scene;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import nl.team3.engine.graphics.ResourceLoader;
import nl.team3.engine.graphics.ShaderProgram;
import nl.team3.engine.graphics.Sprite;
import nl.team3.engine.graphics.SpriteRenderer;
import nl.team3.engine.graphics.Texture;
import nl.team3.engine.graphics.animation.Animation;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glEnable;

import org.joml.Vector2f;

public class TicTacToeScene implements Scene {
    private ShaderProgram spriteShader;
    private SpriteRenderer spriteRenderer;
    private Texture testTexture;
    private Sprite testSprite;

    private float spriteX = 0f;
    private int currentWidth;
    private int currentHeight;

    private Animation movetest = new Animation(
            new Vector2f(100, 100), //startpos
            new Vector2f(400, 200),  //endpos
            new Vector2f(0.1f,0.1f), //startscale
            new Vector2f(0.3f, 0.3f), //endscale
            0, 180, 2f, "ExponentialIn"); //startrot, endrot, duration

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
        testSprite.setScale(0.2f);
        testSprite.setRotation(0f);
        testSprite.setAlpha(1f);

        // Fallback dimensions
        currentWidth = Config.WINDOW_WIDTH;
        currentHeight = Config.WINDOW_HEIGHT;

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void update(float dt) {
        spriteX += 150 * dt;

        // Wrap sprite around the screen using dynamic width
        if (spriteX > currentWidth + (testTexture.getWidth() * testSprite.getScale().x)) {
            spriteX = -(testTexture.getWidth() * testSprite.getScale().x);
        }
      
        movetest.UpdateAnimation(dt);
        testSprite.setTransformation(movetest.getPosition(),movetest.getScale(), movetest.getRotation());

        if(actions.isActionDown("pause")){
            movetest.startAnimation();
        }

        // Center sprite using dynamic height
        testSprite.setPosition(spriteX, currentHeight / 2.0f);
    }


    @Override
    public void render() {
        glClear(GL_COLOR_BUFFER_BIT);
        spriteRenderer.draw(testSprite);
    }

    @Override
    public void resize(int width, int height) {
        // Store new dimensions every time window resizes
        this.currentWidth = width;
        this.currentHeight = height;

        if (spriteRenderer != null) {
            spriteRenderer.setProjection(width, height);
        }
    }

    @Override
    public void cleanup() {
        System.out.println("TicTacToeScene closed");
        testTexture.cleanup();
        spriteShader.cleanup();
    }
}