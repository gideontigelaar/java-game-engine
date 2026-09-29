package nl.team3.engine.graphics;

import nl.team3.engine.assets.AssetManager;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glEnable;

public class SpriteRenderer {
    private final ShaderProgram shader;
    private final Mesh quad;

    // Pre-allocated matrices, avoid allocating every frame
    private final Matrix4f projection = new Matrix4f();
    private final Matrix4f transformMatrix = new Matrix4f();

    public SpriteRenderer(AssetManager assets) {
        this.shader = assets.getShader(AssetManager.SPRITE_SHADER);
        this.quad = assets.getMesh(AssetManager.QUAD_MESH);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public void setProjection(int width, int height) {
        this.projection.identity().ortho(0, width, height, 0, -1, 1);
    }

    public void draw(Sprite sprite) {
        shader.bind();

        Matrix4f model = buildModelMatrix(sprite);
        shader.setUniformMat4("uModel", model);
        shader.setUniformMat4("uProjection", projection);

        Vector4f tint = sprite.getTint();
        float alpha = sprite.getAlpha();
        shader.setUniform4f("uTint", tint.x, tint.y, tint.z, tint.w * alpha);

        shader.setUniform1i("uTexture", 0);
        sprite.getTexture().bind();

        quad.render();

        sprite.getTexture().unbind();
        shader.unbind();
    }

    private Matrix4f buildModelMatrix(Sprite sprite) {
        Texture texture = sprite.getTexture();
        float width = texture.getWidth() * sprite.getScale().x;
        float height = texture.getHeight() * sprite.getScale().y;

        float pivotOffsetX = (0.5f - sprite.getOrigin().x) * width;
        float pivotOffsetY = (0.5f - sprite.getOrigin().y) * height;

        return transformMatrix.identity()
                .translate(sprite.getPosition().x, sprite.getPosition().y, 0)
                .rotateZ(sprite.getRotation())
                .translate(pivotOffsetX, pivotOffsetY, 0)
                .scale(width, height, 1);
    }
}