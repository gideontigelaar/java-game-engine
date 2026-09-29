package nl.team3.engine.graphics;

import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class ResourceLoader {
    private ResourceLoader() {
    }

    public static String readResource(String path) {
        try (InputStream in = ResourceLoader.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IOException("Resource not found on classpath: " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read resource: " + path, e);
        }
    }

    public static ByteBuffer loadResourceAsByteBuffer(String path) {
        try (InputStream in = ResourceLoader.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IOException("Resource not found on classpath: " + path);
            }

            byte[] bytes = in.readAllBytes();
            ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
            buffer.put(bytes);
            buffer.flip();

            return buffer;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read resource into buffer: " + path, e);
        }
    }
}