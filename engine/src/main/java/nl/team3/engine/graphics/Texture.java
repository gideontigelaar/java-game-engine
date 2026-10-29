package nl.team3.engine.graphics;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class Texture {
    private final int textureId;
    private final int width;
    private final int height;

    private Texture(int textureId, int width, int height) {
        this.textureId = textureId;
        this.width = width;
        this.height = height;
    }

    public void bind() {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void cleanup() {
        glDeleteTextures(textureId);
    }

    // Decode encoded image and upload to GPU
    public static Texture create(ByteBuffer imageBuffer, String name) {
        ByteBuffer imageData;
        int imgWidth, imgHeight;

        // Prevent memory leaks
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer widthBuffer = stack.mallocInt(1);
            IntBuffer heightBuffer = stack.mallocInt(1);
            IntBuffer channelsBuffer = stack.mallocInt(1);

            STBImage.stbi_set_flip_vertically_on_load(true);
            imageData = STBImage.stbi_load_from_memory(imageBuffer, widthBuffer, heightBuffer, channelsBuffer, 4);

            if (imageData == null) {
                throw new RuntimeException("Failed to decode texture: " + name + " - " + STBImage.stbi_failure_reason());
            }

            imgWidth = widthBuffer.get(0);
            imgHeight = heightBuffer.get(0);
        }

        Texture texture = create(imgWidth, imgHeight, imageData);
        STBImage.stbi_image_free(imageData);

        return texture;
    }

    public static Texture create(int width, int height, ByteBuffer pixels) {
        int textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        return new Texture(textureId, width, height);
    }

    public static Texture createWhite() {
        ByteBuffer pixel = BufferUtils.createByteBuffer(4);
        pixel.put((byte) 0xFF).put((byte) 0xFF).put((byte) 0xFF).put((byte) 0xFF).flip();
        return create(1, 1, pixel);
    }
}