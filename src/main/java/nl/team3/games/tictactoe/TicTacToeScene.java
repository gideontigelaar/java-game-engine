package nl.team3.games.tictactoe;

import nl.team3.engine.core.Config;
import nl.team3.engine.core.Scene;
import nl.team3.engine.graphics.ResourceLoader;
import nl.team3.engine.graphics.ShaderProgram;
import nl.team3.engine.graphics.Sprite;
import nl.team3.engine.graphics.SpriteRenderer;
import nl.team3.engine.graphics.Texture;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glEnable;

public class TicTacToeScene implements Scene {

    private ShaderProgram spriteShader;
    private SpriteRenderer spriteRenderer;
    private Texture testTexture;
    private Sprite testSprite;

    private float spriteX = 0f;

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

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void update(float dt) {
        spriteX += 100 * dt;
        testSprite.setPosition(spriteX, Config.WINDOW_HEIGHT / 2.0f);
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