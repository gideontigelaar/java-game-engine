package nl.team3.games.tictactoe;

import org.lwjgl.opengl.GL11;
import nl.team3.engine.core.Scene;
import nl.team3.engine.core.Config;

public class TicTacToeScene implements Scene {
    private float x;
    private float y;
    private float speed;

    @Override
    public void init() {
        System.out.println("TicTacToeScene loaded");

        x = 0;
        y = Config.WINDOW_HEIGHT / 2.0f; // Start vertically centered
        speed = 200.0f; // Move 200 pixels per second

        // Switch to 2D view
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        // 1 unit = 1 pixel, (0,0) = top left corner
        GL11.glOrtho(0, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT, 0, -1, 1);
        // Switch back to normal viewing mode
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
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
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);

        // Add a square
        GL11.glBegin(GL11.GL_QUADS);
            GL11.glColor3f(1.0f, 1.0f, 1.0f);
            GL11.glVertex2f(0, 0);
            GL11.glVertex2f(50, 0);
            GL11.glVertex2f(50, 50);
            GL11.glVertex2f(0, 50);
        GL11.glEnd();

        GL11.glPopMatrix();
    }

    @Override
    public void cleanup() {
        System.out.println("TicTacToeScene closed");
    }
}