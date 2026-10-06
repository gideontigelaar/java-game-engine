package nl.team3.engine.assets;

import nl.team3.engine.graphics.Font;
import nl.team3.engine.graphics.Mesh;
import nl.team3.engine.graphics.ShaderProgram;
import nl.team3.engine.graphics.Texture;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class AssetManager {
    public static final String SPRITE_SHADER = "sprite";
    public static final String TEXT_SHADER = "text";
    public static final String QUAD_MESH = "quad";

    private final Map<String, Texture> textures = new HashMap<>();
    private final Map<String, ShaderProgram> shaders = new HashMap<>();
    private final Map<String, Mesh> meshes = new HashMap<>();
    private final Map<String, Font> fonts = new HashMap<>();

    public AssetManager() {
        loadShader(SPRITE_SHADER, "/shaders/sprite.vert", "/shaders/sprite.frag");
        loadShader(TEXT_SHADER, "/shaders/text.vert", "/shaders/text.frag");
        loadMesh(QUAD_MESH, Mesh::createQuad);
    }

    public Texture loadTexture(String key, String path) {
        return textures.computeIfAbsent(key,
                k -> Texture.create(ResourceLoader.loadResourceAsByteBuffer(path), path));
    }

    public Font loadFont(String key, String path, float size) {
        return fonts.computeIfAbsent(key,
                k -> new Font(ResourceLoader.loadResourceAsByteBuffer(path), size));
    }

    public ShaderProgram loadShader(String key, String vertexPath, String fragmentPath) {
        return shaders.computeIfAbsent(key,
                k -> new ShaderProgram(ResourceLoader.readResource(vertexPath), ResourceLoader.readResource(fragmentPath)));
    }

    public Mesh loadMesh(String key, Supplier<Mesh> factory) {
        return meshes.computeIfAbsent(key, k -> factory.get());
    }

    public Texture getTexture(String key) {
        return get(textures, "Texture", key);
    }

    public Font getFont(String key) {
        return get(fonts, "Font", key);
    }

    public ShaderProgram getShader(String key) {
        return get(shaders, "Shader", key);
    }

    public Mesh getMesh(String key) {
        return get(meshes, "Mesh", key);
    }

    private static <T> T get(Map<String, T> assets, String type, String key) {
        T asset = assets.get(key);
        if (asset == null) {
            throw new IllegalStateException(type + " not loaded: " + key);
        }
        return asset;
    }

    public void cleanup() {
        textures.values().forEach(Texture::cleanup);
        shaders.values().forEach(ShaderProgram::cleanup);
        meshes.values().forEach(Mesh::cleanup);
        fonts.values().forEach(Font::cleanup);

        textures.clear();
        shaders.clear();
        meshes.clear();
        fonts.clear();
    }
}