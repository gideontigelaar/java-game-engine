package nl.team3.engine.graphics.animation;

import nl.team3.engine.graphics.Sprite;
import org.joml.Vector2f;

public class Animation {
    private final float startValue;
    private final float endValue;

    private final String easingType;
    private final float duration;
    private final Timer timer;

    private Sprite sprite;


    private boolean started = false;
    private boolean playing = false;
    private float elapsed = 0f;

    public Animation(float startValue, float endValue, String easingType, float duration) {
        this.startValue = startValue;
        this.endValue = endValue;
        this.easingType = easingType;
        this.duration = duration;
        this.timer = new Timer(duration);
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

    public float getValue() {
        float p = getProgress();
        return Interpolators.Easing(startValue, endValue, p, easingType);
    }
}
