package nl.team3.engine.graphics;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Mesh {
    private static final int FLOATS_PER_VERTEX = 5; // x, y, r, g, b

    private final int vaoId;
    private final int vboId;
    private final int vertexCount;
    private final int drawMode;

    public Mesh(float[] vertices, int drawMode) {
        this.vertexCount = vertices.length / FLOATS_PER_VERTEX;
        this.drawMode = drawMode;

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        int stride = FLOATS_PER_VERTEX * Float.BYTES;

        // Position
        glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, 0L);
        glEnableVertexAttribArray(0);

        // Color
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 2L * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void render() {
        glBindVertexArray(vaoId);
        glDrawArrays(drawMode, 0, vertexCount);
        glBindVertexArray(0);
    }

    public void cleanup() {
        glDeleteBuffers(vboId);
        glDeleteVertexArrays(vaoId);
    }
}