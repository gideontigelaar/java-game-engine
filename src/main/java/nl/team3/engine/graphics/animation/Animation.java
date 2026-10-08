package nl.team3.engine.graphics.animation;

import nl.team3.engine.graphics.Sprite;
import org.joml.Vector2f;

public class Animation {

    private final Vector2f startPos;
    private final Vector2f endPos;
    private final Vector2f startScale;
    private final Vector2f endScale;
    private final float startRot;
    private final float endRot;
    private final String easingType;
    private final float duration;
    private final Timer timer;

    private Sprite sprite;

    // playback state
    private boolean started = false;
    private boolean playing = false;
    private float elapsed = 0f;

    private Animation(Builder b) {
        this.startPos = new Vector2f(b.startPos);
        this.endPos = new Vector2f(b.endPos);
        this.startScale = new Vector2f(b.startScale);
        this.endScale = new Vector2f(b.endScale);
        this.startRot = b.startRot;
        this.endRot = b.endRot;
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
                throw new IllegalStateException("duration must not be negative");
            }
            return new Animation(this);
        }
    }




    public void startAnimation() {
        elapsed = 0f;
        started = true;
        playing = true;
        timer.start();
    }


    public void UpdateAnimation(float dt) {
        if (!playing) return;

        elapsed += dt;
        timer.update(dt);

        if (elapsed >= duration) {
            elapsed = duration;
            playing = false;
        }
    }

    public boolean isPlaying()  { return playing; }
    public boolean hasStarted() { return started; }
    public boolean isFinished() { return started && !playing; }


    public float getProgress() {
        if (!started) return 0f;
        if (duration <= 0f) return 1f;
        return Math.min(elapsed / duration, 1f);
    }



    public void setTransformation() {
        if (!started || sprite == null) return;
        sprite.setTransformation(getPosition(), getScale(), getRotation());
    }

    public void setPosition() {
        if (!started || sprite == null) return;
        sprite.setPosition(getPosition());
    }

    public void setScale() {
        if (!started || sprite == null) return;
        sprite.setScale(getScale());
    }

    public void setRotation() {
        if (!started || sprite == null) return;
        sprite.setRotation(getRotation());
    }



    public Vector2f getPosition() {
        float p = getProgress();
        return new Vector2f(
                Interpolators.Easing(startPos.x, endPos.x, p, easingType),
                Interpolators.Easing(startPos.y, endPos.y, p, easingType)
        );
    }

    public Vector2f getScale() {
        float p = getProgress();
        return new Vector2f(
                Interpolators.Easing(startScale.x, endScale.x, p, easingType),
                Interpolators.Easing(startScale.y, endScale.y, p, easingType)
        );
    }


    public float getRotation() {
        float deg = Interpolators.Easing(startRot, endRot, getProgress(), easingType);
        return (float) (deg * (Math.PI / 180));
    }



    public Timer getTimer() { return timer; }

    public void SetSprite(Sprite sprite) {
        this.sprite = sprite;
    }
}