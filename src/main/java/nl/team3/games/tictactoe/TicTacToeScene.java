package nl.team3.games.tictactoe;

import org.joml.Matrix4f;

import nl.team3.engine.core.Scene;
import nl.team3.engine.core.Config;
import nl.team3.engine.graphics.Mesh;
import nl.team3.engine.graphics.ResourceLoader;
import nl.team3.engine.graphics.ShaderProgram;

import static org.lwjgl.opengl.GL11.GL_TRIANGLE_FAN;

public class TicTacToeScene implements Scene {
    private float x;
    private float y;
    private float speed;

    private ShaderProgram shader;
    private Mesh squareMesh;
    private Matrix4f projectionMatrix;

    @Override
    public void init() {
        System.out.println("TicTacToeScene loaded");

        x = 0;
        y = Config.WINDOW_HEIGHT / 2.0f; // Start vertically centered
        speed = 200.0f; // Move 200 pixels per second

        // 1 unit = 1 pixel, (0,0) = top left corner
        projectionMatrix = new Matrix4f().ortho(
                0, Config.WINDOW_WIDTH,
                Config.WINDOW_HEIGHT, 0,
                -1, 1);

        shader = new ShaderProgram(
                ResourceLoader.readResource("/shaders/basic.vert"),
                ResourceLoader.readResource("/shaders/basic.frag"));

        // Square (x, y, r, g, b)
        float[] vertices = {
                0f, 0f, 1f, 1f, 1f,
                50f, 0f, 1f, 1f, 1f,
                50f, 50f, 1f, 1f, 1f,
                0f, 50f, 1f, 1f, 1f,
        };
        squareMesh = new Mesh(vertices, GL_TRIANGLE_FAN);
    }

    @Override
    public void update(float dt) {
        // Move object to the right
        x += speed * dt;

        if (x > Config.WINDOW_WIDTH) {
            x = -50;
        }
    }

    @Override
    public void render() {
        Matrix4f modelMatrix = new Matrix4f().translate(x, y, 0);
        Matrix4f mvp = new Matrix4f(projectionMatrix).mul(modelMatrix);

        shader.bind();
        shader.setUniformMat4("uMVP", mvp);
        squareMesh.render();
        shader.unbind();
    }

    @Override
    public void cleanup() {
        System.out.println("TicTacToeScene closed");
        squareMesh.cleanup();
        shader.cleanup();
    }
}