package nl.team3.engine.graphics;

import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.system.MemoryUtil;
import java.nio.ByteBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.stb.STBTruetype.stbtt_PackBegin;
import static org.lwjgl.stb.STBTruetype.stbtt_PackEnd;
import static org.lwjgl.stb.STBTruetype.stbtt_PackFontRange;

public class Font {
    public static final int FIRST_CHAR = 32;
    public static final int CHAR_COUNT = 96;
    public static final int MAX_OUTLINE = 8;

    private static final int ATLAS_SIZE = 1024;
    private static final int PADDING = 2 * MAX_OUTLINE + 2;

    private final int textureId;
    private final STBTTPackedchar.Buffer charData;
    private final float[] advances = new float[CHAR_COUNT];
    private final float capHeight;
    private final float height;

    public Font(ByteBuffer ttfData, float fontHeight) {
        height = fontHeight;
        charData = STBTTPackedchar.malloc(CHAR_COUNT);
        ByteBuffer alphaBitmap = MemoryUtil.memCalloc(ATLAS_SIZE * ATLAS_SIZE);

        int packOffset = PADDING * ATLAS_SIZE + PADDING;
        int packSize = ATLAS_SIZE - 2 * PADDING;
        ByteBuffer packArea = MemoryUtil.memSlice(alphaBitmap, packOffset, alphaBitmap.capacity() - packOffset);
        boolean packed;
        try (STBTTPackContext context = STBTTPackContext.malloc()) {
            packed = stbtt_PackBegin(context, packArea, packSize, packSize, ATLAS_SIZE, PADDING)
                    && stbtt_PackFontRange(context, ttfData, 0, fontHeight, FIRST_CHAR, charData);
            stbtt_PackEnd(context);
        }
        if (!packed) {
            MemoryUtil.memFree(alphaBitmap);
            charData.free();
            throw new IllegalStateException("Font of size " + fontHeight + " does not fit in its atlas");
        }

        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, ATLAS_SIZE, ATLAS_SIZE, 0, GL_RED, GL_UNSIGNED_BYTE, alphaBitmap);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 4);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        MemoryUtil.memFree(alphaBitmap);

        for (int i = 0; i < advances.length; i++) {
            STBTTPackedchar glyph = charData.get(i);
            glyph.x0((short) (glyph.x0() + PADDING)).x1((short) (glyph.x1() + PADDING));
            glyph.y0((short) (glyph.y0() + PADDING)).y1((short) (glyph.y1() + PADDING));
            advances[i] = glyph.xadvance();
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

    public float getHeight() {
        return height;
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

    public STBTTPackedchar.Buffer getCharData() {
        return charData;
    }

    public void cleanup() {
        glDeleteTextures(textureId);
        charData.free();
    }
}