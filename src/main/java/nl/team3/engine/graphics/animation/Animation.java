package nl.team3.engine.graphics.animation;

import nl.team3.engine.graphics.Sprite;
import org.joml.Vector2f;

public class Animation {

    final private Vector2f startPos;
    final private Vector2f endPos;
    final private Vector2f startScale;
    final private Vector2f endScale;
    final private String easingType;
    final private int startRot;
    final private int endRot;
    final private Timer timer;
    private float duration;
    private Sprite sprite;

    private Animation(Builder b) {
        this.startPos = new Vector2f(b.startPos);   // kopieën, zodat aanpassen buiten de klasse niets kapotmaakt
        this.endPos = new Vector2f(b.endPos);
        this.startScale = new Vector2f(b.startScale);
        this.endScale = new Vector2f(b.endScale);
        this.startRot = (int) b.startRot;
        this.endRot = (int) b.endRot;
        this.duration = b.duration;
        this.easingType = b.easingType;
        this.sprite = b.sprite;
        this.timer = new Timer(this.duration);
    }

    public static Builder builder() {
        return new Builder();
    }

    // ---------- Builder ----------
    public static class Builder {
        private Vector2f startPos = new Vector2f(0, 0);
        private Vector2f endPos = new Vector2f(0, 0);
        private Vector2f startScale = new Vector2f(1, 1);
        private Vector2f endScale = new Vector2f(1, 1);
        private float startRot = 0f;
        private float endRot = 0f;
        private float duration = 1f;
        private String easingType = "Linear";
        private Sprite sprite;   // optioneel

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

        public Builder easing(String easingType) {
            this.easingType = easingType;
            return this;
        }

        public Builder sprite(Sprite sprite) {
            this.sprite = sprite;
            return this;
        }

        public Animation build() {
            if (duration < 0f) {
                throw new IllegalStateException("duration mag niet negatief zijn");
            }
            return new Animation(this);
        }
    }

    public void UpdateAnimation(float dt) {
        timer.update(dt);

    }
    public void setTransformation(){
        sprite.setTransformation(getPosition(),getScale(), getRotation());
    }
    public void setPosition(){
        sprite.setPosition(getPosition());
    }
    public void setScale(){
        sprite.setScale(getScale());
    }
    public void setRotation(){
        sprite.setRotation(getRotation());
    }


    public Vector2f getPosition() {
        return new Vector2f(
                Interpolators.Easing(this.startPos.get(0), this.endPos.get(0), timer.getProgress(), easingType),
                Interpolators.Easing(this.startPos.get(1), this.endPos.get(1), timer.getProgress(), easingType)
        );
    }

    public Vector2f getScale() {
        return new Vector2f(
                Interpolators.Easing(this.startScale.get(0), this.endScale.get(0), timer.getProgress(), easingType),
                Interpolators.Easing(this.startScale.get(1), this.endScale.get(1), timer.getProgress(), easingType)
        );
    }

    public float getRotation() {
        return (float) (Interpolators.Easing(this.startRot, this.endRot, timer.getProgress(), easingType) * (Math.PI / 180));
    }

    public void startAnimation(){
        this.timer.start();
    }

    public Timer getTimer(){return timer;}

    public void SetSprite(Sprite sprite){
        this.sprite = sprite;
    }
}