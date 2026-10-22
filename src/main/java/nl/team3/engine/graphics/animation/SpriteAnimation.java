package nl.team3.engine.graphics.animation;

import nl.team3.engine.graphics.Sprite;
import org.joml.Vector2f;

public class SpriteAnimation extends Animation {
    private final Vector2f startPos;
    private final Vector2f endPos;
    private final Vector2f startScale;
    private final Vector2f endScale;
    private final float startRot;
    private final float endRot;
    private final Sprite sprite;

    private SpriteAnimation(Builder b) {
        super(0f, 1f, b.easingType, b.duration);
        this.startPos = new Vector2f(b.startPos);
        this.endPos = new Vector2f(b.endPos);
        this.startScale = new Vector2f(b.startScale);
        this.endScale = new Vector2f(b.endScale);
        this.startRot = b.startRot;
        this.endRot = b.endRot;
        this.sprite = b.sprite;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Vector2f startPos = new Vector2f(0, 0);
        private Vector2f endPos = new Vector2f(0, 0);
        private Vector2f startScale = new Vector2f(1, 1);
        private Vector2f endScale = new Vector2f(1, 1);
        private float startRot = 0f;
        private float endRot = 0f;
        private float duration = 1f;
        private EasingType easingType = EasingType.LINEAR;
        private Sprite sprite;   // optional

        private Builder() {}

        public Builder position(Vector2f start, Vector2f end) {
            this.startPos = start;
            this.endPos = end;
            return this;
        }

        public Builder scale(Vector2f start, Vector2f end) {
            this.startScale = start;
            this.endScale = end;
            return this;
        }

        public Builder rotation(float start, float end) {
            this.startRot = start;
            this.endRot = end;
            return this;
        }

        public Builder duration(float seconds) {
            this.duration = seconds;
            return this;
        }

        public Builder easing(EasingType easingType) {
            this.easingType = easingType;
            return this;
        }

        public Builder sprite(Sprite sprite) {
            this.sprite = sprite;
            return this;
        }

        public SpriteAnimation build() {
            if (duration < 0f) {
                throw new IllegalStateException("duration must not be negative");
            }
            return new SpriteAnimation(this);
        }
    }

    public void setTransformation() {
        setPosition();
        setScale();
        setRotation();
    }

    public void setPosition() {
        if (!hasStarted() || sprite == null) return;
        float amount = getValue();
        sprite.setPosition(lerp(startPos.x, endPos.x, amount), lerp(startPos.y, endPos.y, amount));
    }

    public void setScale() {
        if (!hasStarted() || sprite == null) return;
        float amount = getValue();
        sprite.setScale(lerp(startScale.x, endScale.x, amount), lerp(startScale.y, endScale.y, amount));
    }

    public void setRotation() {
        if (!hasStarted() || sprite == null) return;
        sprite.setRotation(getRotation());
    }

    public Vector2f getPosition() {
        float amount = getValue();
        return new Vector2f(lerp(startPos.x, endPos.x, amount), lerp(startPos.y, endPos.y, amount));
    }

    public Vector2f getScale() {
        float amount = getValue();
        return new Vector2f(lerp(startScale.x, endScale.x, amount), lerp(startScale.y, endScale.y, amount));
    }

    public float getRotation() {
        float deg = lerp(startRot, endRot, getValue());
        return (float) (deg * (Math.PI / 180));
    }

    private static float lerp(float start, float end, float amount) {
        return amount >= 1f ? end : start + (end - start) * amount;
    }
}