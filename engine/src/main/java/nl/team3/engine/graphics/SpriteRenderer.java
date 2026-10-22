package nl.team3.engine.graphics;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.Map;

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

    // Nine-slice meshes
    private final Map<SliceUv, Mesh> sliceMeshes = new HashMap<>();

    private record SliceUv(float u0, float v0, float u1, float v1) {}

    public SpriteRenderer(ShaderProgram shader, Mesh quad) {
        this.shader = shader;
        this.quad = quad;

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        shader.bind();
        shader.setUniform1i("uTexture", 0);
    }

    public void setProjection(int width, int height) {
        this.projection.identity().ortho(0, width, height, 0, -1, 1);
        shader.bind();
        shader.setUniformMat4("uProjection", projection);
    }

    public void draw(Sprite sprite) {
        Texture texture = sprite.getTexture();

        shader.bind();
        shader.setUniformMat4("uModel", buildModelMatrix(sprite, texture));

        Vector4f tint = sprite.getTint();
        shader.setUniform4f("uTint", tint.x, tint.y, tint.z, tint.w * sprite.getAlpha());

        texture.bind();
        quad.render();
    }

    public void drawNineSlice(Sprite sprite, float x, float y, float targetWidth, float targetHeight,
                              NineSlice slice, float pixelScale) {

        x -= sprite.getOrigin().x * targetWidth;
        y -= sprite.getOrigin().y * targetHeight;

        Texture texture = sprite.getTexture();
        float texW = texture.getWidth();
        float texH = texture.getHeight();

        float leftW   = Math.min(slice.left()   * pixelScale, targetWidth  * 0.5f);
        float rightW  = Math.min(slice.right()  * pixelScale, targetWidth  * 0.5f);
        float topH    = Math.min(slice.top()    * pixelScale, targetHeight * 0.5f);
        float bottomH = Math.min(slice.bottom() * pixelScale, targetHeight * 0.5f);

        // Screen positions according to 3x3 grid
        float[] destX = { x, x + leftW, x + targetWidth - rightW, x + targetWidth };
        float[] destY = { y, y + topH, y + targetHeight - bottomH, y + targetHeight };

        float uLeft = slice.left() / texW;
        float uRight = (texW - slice.right()) / texW;
        float vTop = slice.top() / texH;
        float vBottom = (texH - slice.bottom()) / texH;

        float[] uvsX = { 0.0f, uLeft, uRight, 1.0f };
        float[] uvsY = { 0.0f, vTop, vBottom, 1.0f };

        shader.bind();

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

                Mesh segmentMesh = sliceMeshes.computeIfAbsent(
                        new SliceUv(uvsX[col], 1f - uvsY[row + 1], uvsX[col + 1], 1f - uvsY[row]),
                        uv -> Mesh.createQuad(uv.u0(), uv.v0(), uv.u1(), uv.v1()));

                transformMatrix.identity()
                        .translate(segX + segW * 0.5f, segY + segH * 0.5f, 0)
                        .scale(segW, segH, 1);

                shader.setUniformMat4("uModel", transformMatrix);
                segmentMesh.render();
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

    public void cleanup() {
        sliceMeshes.values().forEach(Mesh::cleanup);
        sliceMeshes.clear();
    }
}