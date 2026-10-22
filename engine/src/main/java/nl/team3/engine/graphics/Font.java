package nl.team3.engine.graphics;

import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.system.MemoryUtil;
import java.nio.ByteBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBTruetype.stbtt_BakeFontBitmap;

public class Font {
    public static final int FIRST_CHAR = 32;
    public static final int CHAR_COUNT = 96;

    private static final int ATLAS_SIZE = 512;

    private final int textureId;
    private final STBTTBakedChar.Buffer charData;
    private final float[] advances = new float[CHAR_COUNT];
    private final float capHeight;

    public Font(ByteBuffer ttfData, float fontHeight) {
        charData = STBTTBakedChar.malloc(CHAR_COUNT);
        ByteBuffer alphaBitmap = MemoryUtil.memAlloc(ATLAS_SIZE * ATLAS_SIZE);

        stbtt_BakeFontBitmap(ttfData, fontHeight, alphaBitmap, ATLAS_SIZE, ATLAS_SIZE, FIRST_CHAR, charData);

        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, ATLAS_SIZE, ATLAS_SIZE, 0, GL_RED, GL_UNSIGNED_BYTE, alphaBitmap);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 4);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        MemoryUtil.memFree(alphaBitmap);

        for (int i = 0; i < advances.length; i++) {
            advances[i] = charData.get(i).xadvance();
        }
        capHeight = -charData.get('H' - FIRST_CHAR).yoff();
    }

    public float getTextWidth(String text) {
        float width = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= FIRST_CHAR && c < FIRST_CHAR + CHAR_COUNT) {
                width += advances[c - FIRST_CHAR];
            }
        }
        return width;
    }

    public float getCapHeight() {
        return capHeight;
    }

    public int getTextureId() {
        return textureId;
    }

    public int getAtlasWidth() {
        return ATLAS_SIZE;
    }

    public int getAtlasHeight() {
        return ATLAS_SIZE;
    }

    public STBTTBakedChar.Buffer getCharData() {
        return charData;
    }

    public void cleanup() {
        glDeleteTextures(textureId);
        charData.free();
    }
}