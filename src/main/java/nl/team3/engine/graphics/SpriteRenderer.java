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

    private final Matrix4f projection = new Matrix4f();
    private final Matrix4f transformMatrix = new Matrix4f();

    public SpriteRenderer(AssetManager assets) {
        this.shader = assets.getShader(AssetManager.SPRITE_SHADER);
        this.quad = assets.getMesh(AssetManager.QUAD_MESH);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        shader.bind();
        shader.setUniform1i("uTexture", 0);
    }

    public void setProjection(int width, int height) {
        this.projection.identity().ortho(0, width, height, 0, -1, 1);
    }

    public void draw(Sprite sprite) {
        Texture texture = sprite.getTexture();

        shader.bind();
        shader.setUniformMat4("uModel", buildModelMatrix(sprite, texture));
        shader.setUniformMat4("uProjection", projection);

        Vector4f tint = sprite.getTint();
        shader.setUniform4f("uTint", tint.x, tint.y, tint.z, tint.w * sprite.getAlpha());

        texture.bind();
        quad.render();
    }

    public void drawNineSlice(Sprite sprite, float x, float y, float targetWidth, float targetHeight, NineSlice slice) {
        Texture texture = sprite.getTexture();
        float texW = texture.getWidth();
        float texH = texture.getHeight();

        float leftW = Math.min(slice.left, targetWidth * 0.5f);
        float rightW = Math.min(slice.right, targetWidth * 0.5f);
        float topH = Math.min(slice.top, targetHeight * 0.5f);
        float bottomH = Math.min(slice.bottom, targetHeight * 0.5f);

        // Screen positions according to 3x3 grid
        float[] destX = { x, x + leftW, x + targetWidth - rightW, x + targetWidth };
        float[] destY = { y, y + bottomH, y + targetHeight - topH, y + targetHeight };

        float uLeft = slice.left / texW;
        float uRight = (texW - slice.right) / texW;
        float vTop = slice.top / texH;
        float vBottom = (texH - slice.bottom) / texH;

        float[] uvsX = { 0.0f, uLeft, uRight, 1.0f };
        float[] uvsY = { 0.0f, vTop, vBottom, 1.0f };

        shader.bind();
        shader.setUniformMat4("uProjection", projection);

        Vector4f tint = sprite.getTint();
        shader.setUniform4f("uTint", tint.x, tint.y, tint.z, tint.w * sprite.getAlpha());
        texture.bind();

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                float segX = destX[col];
                float segY = destY[row];
                float segW = destX[col + 1] - destX[col];
                float segH = destY[row + 1] - destY[row];

                if (segW <= 0 || segH <= 0) continue;

                Mesh segmentMesh = Mesh.createQuad(uvsX[col], uvsY[row], uvsX[col + 1], uvsY[row + 1]);

                Matrix4f model = new Matrix4f()
                        .translate(segX + segW * 0.5f, segY + segH * 0.5f, 0)
                        .scale(segW, segH, 1);

                shader.setUniformMat4("uModel", model);
                segmentMesh.render();

                segmentMesh.cleanup();
            }
        }
    }

    private Matrix4f buildModelMatrix(Sprite sprite, Texture texture) {
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