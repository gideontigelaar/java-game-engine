package nl.team3.engine.graphics;

import org.joml.Matrix4f;
import org.joml.Vector4f;

public class SpriteRenderer {
    private final ShaderProgram shader;
    private final Mesh quad;

    // Pre-allocated matrices, avoid allocating every frame
    private final Matrix4f projection = new Matrix4f();
    private final Matrix4f transformMatrix = new Matrix4f();

    public SpriteRenderer(ShaderProgram shader, int viewportWidth, int viewportHeight) {
        this.shader = shader;
        this.quad = Mesh.getQuad();
        setProjection(viewportWidth, viewportHeight);
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