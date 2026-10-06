package nl.team3.engine.graphics;

import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.system.MemoryUtil;
import java.nio.ByteBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBTruetype.stbtt_BakeFontBitmap;

public class Font {
    private final int textureId;
    private final int atlasWidth = 512;
    private final int atlasHeight = 512;
    private final STBTTBakedChar.Buffer charData;

    public Font(ByteBuffer ttfData, float fontHeight) {
        charData = STBTTBakedChar.malloc(96);
        ByteBuffer alphaBitmap = MemoryUtil.memAlloc(atlasWidth * atlasHeight);

        stbtt_BakeFontBitmap(ttfData, fontHeight, alphaBitmap, atlasWidth, atlasHeight, 32, charData);

        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, atlasWidth, atlasHeight, 0, GL_RED, GL_UNSIGNED_BYTE, alphaBitmap);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 4);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        MemoryUtil.memFree(alphaBitmap);
    }

    public int getTextureId() {
        return textureId;
    }

    public int getAtlasWidth() {
        return atlasWidth;
    }

    public int getAtlasHeight() {
        return atlasHeight;
    }

    public STBTTBakedChar.Buffer getCharData() {
        return charData;
    }

    public void cleanup() {
        glDeleteTextures(textureId);
        charData.free();
    }
}