package nl.team3.engine.graphics;

public class SpriteRenderer {
    private final Mesh quad;
    private final ShaderProgram shader;

    public SpriteRenderer(ShaderProgram shader) {
        this.shader = shader;
        this.quad = Mesh.createQuad();
    }

    public void draw(Sprite sprite) {
        sprite.getTexture().bind();
        quad.render();
    }
}
