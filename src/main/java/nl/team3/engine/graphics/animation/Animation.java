package nl.team3.engine.graphics.animation;

public class Animation {
    private final float startValue;
    private final float endValue;

    private final String easingType;
    private final float duration;

    private boolean started = false;
    private boolean playing = false;
    private float elapsed = 0f;

    public Animation(float startValue, float endValue, String easingType, float duration) {
        this.startValue = startValue;
        this.endValue = endValue;
        this.easingType = easingType;
        this.duration = duration;
    }

    public void startAnimation() {
        elapsed = 0f;
        started = true;
        playing = true;
    }


    public void UpdateAnimation(float dt) {
        if (!playing) return;

        elapsed += dt;

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