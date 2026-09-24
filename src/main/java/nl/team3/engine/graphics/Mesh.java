package nl.team3.engine.graphics;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Mesh {
    private static final int FLOATS_PER_VERTEX = 4; // x, y, u, v

    private static Mesh quadInstance;

    private final int vaoId;
    private final int vboId;
    private final int eboId;
    private final int indexCount;

    public Mesh(float[] vertices, int[] indices) {
        this.indexCount = indices.length;

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        eboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        int stride = FLOATS_PER_VERTEX * Float.BYTES;

        // Position (x, y)
        glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, 0L);
        glEnableVertexAttribArray(0);

        // UV (u, v)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, 2L * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public static Mesh getQuad() {
        if (quadInstance == null) {
            quadInstance = createQuad();
        }
        return quadInstance;
    }

    public static Mesh createQuad() {
        float[] vertices = {
                // pos            // uv
                -0.5f,  0.5f,     0.0f, 1.0f, // top-left
                -0.5f, -0.5f,     0.0f, 0.0f, // bottom-left
                0.5f, -0.5f,     1.0f, 0.0f, // bottom-right
                0.5f,  0.5f,     1.0f, 1.0f  // top-right
        };
        int[] indices = { 0, 1, 2, 2, 3, 0 };
        return new Mesh(vertices, indices);
    }

    public void render() {
        glBindVertexArray(vaoId);
        glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    public void cleanup() {
        glDeleteBuffers(vboId);
        glDeleteBuffers(eboId);
        glDeleteVertexArrays(vaoId);
    }
}