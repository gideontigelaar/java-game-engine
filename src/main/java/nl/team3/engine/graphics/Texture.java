package nl.team3.engine.graphics;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class Texture {
    private final int textureId; // OpenGL's handle to the GPU texture
    private final int width;
    private final int height;

    public Texture(int textureId, int width, int height) {
        this.textureId = textureId;
        this.width = width;
        this.height = height;
    }

    public void bind() {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public void cleanup() {
        glDeleteTextures(textureId);
    }

    public static Texture load(String filePath) {

        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);

        ByteBuffer imageData = STBImage.stbi_load(filePath, width, height, channels, 4); // convert image to bytes

        if (imageData == null) {
            throw new RuntimeException("Failed to load texture: " + filePath + " - " + STBImage.stbi_failure_reason());
        }

        int textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);

        glTexImage2D(
                GL_TEXTURE_2D, 0, GL_RGBA,
                width.get(0), height.get(0),
                0, GL_RGBA, GL_UNSIGNED_BYTE,
                imageData
        );
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        STBImage.stbi_image_free(imageData);

        return new Texture(textureId, width.get(0), height.get(0));
    }
}