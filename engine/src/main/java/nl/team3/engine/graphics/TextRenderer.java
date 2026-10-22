package nl.team3.engine.graphics;

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
    private FloatBuffer vertices = MemoryUtil.memAllocFloat(64 * 24);

    public TextRenderer(ShaderProgram shader) {
        this.shader = shader;
        vaoId = glGenVertexArrays();
        vboId = glGenBuffers();

        glBindVertexArray(vaoId);
        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        glBufferData(GL_ARRAY_BUFFER, 0, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, 4, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        shader.bind();
        shader.setUniform1i("uTexture", 0);
    }

    public void setProjection(int width, int height) {
        projection.identity().ortho(0, width, height, 0, -1, 1);
        shader.bind();
        shader.setUniformMat4("uProjection", projection);
    }

    public void drawText(Font font, String text, float x, float y, Vector4f color) {
        drawText(font, text, x, y, 1f, color);
    }

    public void drawText(Font font, String text, float x, float y, float scale, Vector4f color) {
        if (text.isEmpty()) {
            return;
        }

        int needed = text.length() * 24;
        if (vertices.capacity() < needed) {
            MemoryUtil.memFree(vertices);
            vertices = MemoryUtil.memAllocFloat(Math.max(needed, vertices.capacity() * 2));
        }
        vertices.clear();

        shader.bind();
        shader.setUniform4f("uColor", color.x, color.y, color.z, color.w);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, font.getTextureId());

        glBindVertexArray(vaoId);
        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer xBuffer = stack.floats(x);
            FloatBuffer yBuffer = stack.floats(y);
            STBTTAlignedQuad q = STBTTAlignedQuad.malloc(stack);

            for (int i = 0; i < text.length(); i++) {
                int c = text.charAt(i);
                if (c >= Font.FIRST_CHAR && c < Font.FIRST_CHAR + Font.CHAR_COUNT) {
                    stbtt_GetBakedQuad(font.getCharData(), font.getAtlasWidth(), font.getAtlasHeight(),
                            c - Font.FIRST_CHAR, xBuffer, yBuffer, q, true);

                    float x0 = x + (q.x0() - x) * scale;
                    float y0 = y + (q.y0() - y) * scale;
                    float x1 = x + (q.x1() - x) * scale;
                    float y1 = y + (q.y1() - y) * scale;

                    vertices.put(x0).put(y0).put(q.s0()).put(q.t0());
                    vertices.put(x0).put(y1).put(q.s0()).put(q.t1());
                    vertices.put(x1).put(y1).put(q.s1()).put(q.t1());

                    vertices.put(x0).put(y0).put(q.s0()).put(q.t0());
                    vertices.put(x1).put(y1).put(q.s1()).put(q.t1());
                    vertices.put(x1).put(y0).put(q.s1()).put(q.t0());
                }
            }

            vertices.flip();
            int vertexCount = vertices.remaining() / 4;

            glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_DRAW);
            glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        }
    }

    public void cleanup() {
        glDeleteBuffers(vboId);
        glDeleteVertexArrays(vaoId);
        MemoryUtil.memFree(vertices);
    }
}