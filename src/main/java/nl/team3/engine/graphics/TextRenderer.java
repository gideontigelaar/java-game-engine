package nl.team3.engine.graphics;

import nl.team3.engine.assets.AssetManager;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBTruetype.stbtt_GetBakedQuad;

public class TextRenderer {
    private final ShaderProgram shader;
    private final int vaoId;
    private final int vboId;
    private final Matrix4f projection = new Matrix4f();

    public TextRenderer(AssetManager assets) {
        this.shader = assets.getShader(AssetManager.TEXT_SHADER);
        vaoId = glGenVertexArrays();
        vboId = glGenBuffers();

        glBindVertexArray(vaoId);
        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        glBufferData(GL_ARRAY_BUFFER, 0, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, 4, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void setProjection(int width, int height) {
        projection.identity().ortho(0, width, height, 0, -1, 1);
    }

    public void drawText(Font font, String text, float x, float y, Vector4f color) {
        shader.bind();
        shader.setUniformMat4("uProjection", projection);
        shader.setUniform4f("uColor", color.x, color.y, color.z, color.w);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, font.getTextureId());
        shader.setUniform1i("uTexture", 0);

        glBindVertexArray(vaoId);
        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer xBuffer = stack.floats(x);
            FloatBuffer yBuffer = stack.floats(y);
            STBTTAlignedQuad q = STBTTAlignedQuad.malloc(stack);

            FloatBuffer vertices = MemoryUtil.memAllocFloat(text.length() * 24);

            for (int i = 0; i < text.length(); i++) {
                int c = text.charAt(i);
                if (c >= 32 && c < 128) {
                    stbtt_GetBakedQuad(font.getCharData(), font.getAtlasWidth(), font.getAtlasHeight(),
                            c - 32, xBuffer, yBuffer, q, true);

                    vertices.put(q.x0()).put(q.y0()).put(q.s0()).put(q.t0());
                    vertices.put(q.x0()).put(q.y1()).put(q.s0()).put(q.t1());
                    vertices.put(q.x1()).put(q.y1()).put(q.s1()).put(q.t1());

                    vertices.put(q.x0()).put(q.y0()).put(q.s0()).put(q.t0());
                    vertices.put(q.x1()).put(q.y1()).put(q.s1()).put(q.t1());
                    vertices.put(q.x1()).put(q.y0()).put(q.s1()).put(q.t0());
                }
            }

            vertices.flip();
            int vertexCount = vertices.remaining() / 4;

            glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_DRAW);

            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glDrawArrays(GL_TRIANGLES, 0, vertexCount);

            MemoryUtil.memFree(vertices);
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        shader.unbind();
    }

    public void cleanup() {
        glDeleteBuffers(vboId);
        glDeleteVertexArrays(vaoId);
    }
}