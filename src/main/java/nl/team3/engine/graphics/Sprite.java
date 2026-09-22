package nl.team3.engine.graphics;

import org.joml.Vector2f;
import org.joml.Vector4f;

public class Sprite {

    private Texture texture;

    private Vector2f position = new Vector2f(0, 0);
    private Vector2f scale = new Vector2f(1, 1);
    private float rotation = 0f; // radians
    private float alpha = 1f;

    private Vector2f origin = new Vector2f(0.5f, 0.5f); // pivot, 0-1 range (0.5 = center)
    private Vector4f tint = new Vector4f(1, 1, 1, 1); // color multiplier (r, g, b, a)

    public Sprite(Texture texture) {
        this.texture = texture;
    }

    public Sprite(Texture texture, Vector2f position) {
        this.texture = texture;
        this.position = position;
    }



    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }



    public Vector2f getPosition() {
        return position;
    }

    public void setPosition(Vector2f position) {
        this.position = position;
    }

    public void setPosition(float x, float y) {
        this.position.set(x, y);
    }



    public Vector2f getScale() {
        return scale;
    }

    public void setScale(Vector2f scale) {
        this.scale = scale;
    }

    public void setScale(float x, float y) {
        this.scale.set(x, y);
    }

    public void setScale(float uniform) {
        this.scale.set(uniform, uniform);
    }



    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }



    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = Math.max(0f, Math.min(1f, alpha)); // clamp 0-1
    }



    public Vector2f getOrigin() {
        return origin;
    }

    public void setOrigin(Vector2f origin) {
        this.origin = origin;
    }

    public void setOrigin(float x, float y) {
        this.origin.set(x, y);
    }



    public Vector4f getTint() {
        return tint;
    }

    public void setTint(Vector4f tint) {
        this.tint = tint;
    }

    public void setTint(float r, float g, float b, float a) {
        this.tint.set(r, g, b, a);
    }
}