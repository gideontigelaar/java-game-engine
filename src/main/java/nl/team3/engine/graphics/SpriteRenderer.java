package nl.team3.engine.graphics;

import org.joml.Matrix4f;
import org.joml.Vector4f;

public class SpriteRenderer {

    private final ShaderProgram shader;
    private final Mesh quad;
    private Matrix4f projection;

    public SpriteRenderer(ShaderProgram shader, int viewportWidth, int viewportHeight) {
        this.shader = shader;
        this.quad = Mesh.getQuad();
        setProjection(viewportWidth, viewportHeight);
    }

    public void setProjection(int width, int height) {
        this.projection = new Matrix4f().ortho(0, width, height, 0, -1, 1);
    }

    public void draw(Sprite sprite) {
        shader.bind();

        Matrix4f model = buildModelMatrix(sprite);

        shader.setUniformMat4("uModel", model);
        shader.setUniformMat4("uProjection", projection);
        shader.setUniform1f("uAlpha", sprite.getAlpha());

        Vector4f tint = sprite.getTint();
        shader.setUniform4f("uTint", tint.x, tint.y, tint.z, tint.w);

        shader.setUniform1i("uTexture", 0); // texture unit 0

        sprite.getTexture().bind();
        quad.render();
        sprite.getTexture().unbind();

        shader.unbind();
    }

    private Matrix4f buildModelMatrix(Sprite sprite) {
        Texture tex = sprite.getTexture();
        float width = tex.getWidth() * sprite.getScale().x;
        float height = tex.getHeight() * sprite.getScale().y;

        // offset so rotation/scale happen around the origin (pivot), not the quad's corner
        float pivotOffsetX = (0.5f - sprite.getOrigin().x) * width;
        float pivotOffsetY = (0.5f - sprite.getOrigin().y) * height;

        return new Matrix4f()
                .translate(sprite.getPosition().x, sprite.getPosition().y, 0)
                .rotateZ(sprite.getRotation())
                .translate(pivotOffsetX, pivotOffsetY, 0)
                .scale(width, height, 1);
    }
}