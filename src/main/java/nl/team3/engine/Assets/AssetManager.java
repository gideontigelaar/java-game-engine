package nl.team3.engine.Assets;

import nl.team3.engine.graphics.Mesh;
import nl.team3.engine.graphics.ResourceLoader;
import nl.team3.engine.graphics.ShaderProgram;
import nl.team3.engine.graphics.Texture;

import java.util.HashMap;
import java.util.Map;

public class AssetManager {

    private final Map<String, Texture> textures = new HashMap<>();
    private final Map<String, ShaderProgram> shaders = new HashMap<>();
    private final Map<String, Mesh> meshes = new HashMap<>();

    public void loadTexture(String key, String path) {
        if (textures.containsKey(key)) return;
        Texture texture = Texture.load(path);
        textures.put(key, texture);
    }

    public void loadShader(String key, String vertexPath, String fragmentPath) {
        if (shaders.containsKey(key)) return;
        String vertexSource = ResourceLoader.readResource(vertexPath);
        String fragmentSource = ResourceLoader.readResource(fragmentPath);
        ShaderProgram shader = new ShaderProgram(vertexSource, fragmentSource);
        shaders.put(key, shader);
    }

    public void addMesh(String key, Mesh mesh) {
        if (meshes.containsKey(key)) return;
        meshes.put(key, mesh);
    }

    // Getters

    public Texture getTexture(String key) {
        Texture t = textures.get(key);
        if (t == null) throw new IllegalStateException("Texture niet geladen: " + key);
        return t;
    }

    public ShaderProgram getShader(String key) {
        ShaderProgram s = shaders.get(key);
        if (s == null) throw new IllegalStateException("Shader niet geladen: " + key);
        return s;
    }

    public Mesh getMesh(String key) {
        Mesh m = meshes.get(key);
        if (m == null) throw new IllegalStateException("Mesh niet geregistreerd: " + key);
        return m;
    }

    // Opruim

    public void dispose() {
        textures.values().forEach(Texture::dispose);
        shaders.values().forEach(ShaderProgram::cleanup);
        meshes.values().forEach(Mesh::cleanup);
        textures.clear();
        shaders.clear();
        meshes.clear();
    }
}