package nl.team3.games.tictactoe;

import nl.team3.engine.graphics.*;
import nl.team3.engine.core.Scene;
import nl.team3.engine.core.Config;

import static org.lwjgl.opengl.GL11C.*;

public class TicTacToeScene implements Scene {

    private ShaderProgram spriteShader;
    private SpriteRenderer spriteRenderer;
    private Texture testTexture;
    private Sprite testSprite;
    int x = 0;

    @Override
    public void init() {
        System.out.println("TicTacToeScene loaded");

        String vertexSource = ResourceLoader.readResource("/shaders/sprite.vert");
        String fragmentSource = ResourceLoader.readResource("/shaders/sprite.frag");
        spriteShader = new ShaderProgram(vertexSource, fragmentSource);

        testTexture = Texture.load("src/main/resources/images/testpng.png");

        spriteRenderer = new SpriteRenderer(spriteShader, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);

        testSprite = new Sprite(testTexture);
        testSprite.setPosition(200, 150);
        testSprite.setScale(0.1f);
        testSprite.setRotation(0f);
        testSprite.setAlpha(1f);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void update(float dt) {
        x += 100 * dt;
        testSprite.setPosition(x, Config.WINDOW_HEIGHT / 2);
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
}