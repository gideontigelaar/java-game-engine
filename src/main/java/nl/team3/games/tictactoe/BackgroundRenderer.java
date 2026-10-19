package nl.team3.games.tictactoe;

import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.graphics.Mesh;
import nl.team3.engine.graphics.ShaderProgram;

public class BackgroundRenderer {
    public static final int PALETTE_PINK = 0;
    public static final int PALETTE_GREEN = 1;
    public static final int PALETTE_BLUE = 2;

    private static final String SHADER_KEY = "background";
    private static final String VERTEX_PATH = "/shaders/tictactoe/background.vert";
    private static final String FRAGMENT_PATH = "/shaders/tictactoe/background.frag";
    private static final int PALETTE_COUNT = 3;

    private final ShaderProgram shader;
    private final Mesh quad;
    private float time = 0f;
    private int palette = PALETTE_BLUE;
    private int previousPalette = PALETTE_BLUE;
    private float startTime = 0f;
    private float mouseX;
    private float mouseY;

    public BackgroundRenderer(AssetManager assets) {
        this.shader = assets.loadShader(SHADER_KEY, VERTEX_PATH, FRAGMENT_PATH);
        this.quad = assets.getMesh(AssetManager.QUAD_MESH);
    }

    public void update(float dt) {
        time += dt;
    }

    public void nextPalette(float x, float y) {
        previousPalette = palette;
        palette = (palette + 1) % PALETTE_COUNT;
        mouseX = x;
        mouseY = y;
        startTime = time;
    }
    public void setPalette(float x, float y, int num) {
        previousPalette = palette;
        palette = num;
        mouseX = x;
        mouseY = y;
        startTime = time;
    }

    public void render(int width, int height) {
        shader.bind();
        shader.setUniform1f("iTime", time);
        shader.setUniform2f("iResolution", width, height);
        shader.setUniform1i("palette", palette);
        shader.setUniform1i("previousPalette", previousPalette);
        shader.setUniform1f("startTime", startTime);
        shader.setUniform2f("mousePosition", mouseX, mouseY);
        quad.render();
        shader.unbind();
    }
}