package nl.team3.engine.graphics.animation;

import java.util.List;

public class Timer {
    private float duration;
    private float elapsed = 0f;
    private boolean running = false;
    private boolean finished = false;
    private Runnable onComplete;


    public Timer(float duration) {
        this.duration = duration;
    }

    public Timer onComplete(Runnable callback) {
        this.onComplete = callback;
        return this;
    }

    public void start() {
        elapsed = 0f;
        running = true;
        finished = false;
    }

    public void stop() {
        running = false;
    }

    public void reset() {
        elapsed = 0f;
        finished = false;
    }

    public void update(float dt) {
        if (!running || finished) return;

        elapsed += dt;

        if (elapsed >= duration) {
            elapsed = duration;
            running = false;
            finished = true;
            if (onComplete != null) onComplete.run();
        }


    }

    public float getElapsed() {
        return elapsed;
    }

    public float getRemaining() {
        return Math.max(0f, duration - elapsed);
    }

    public float getProgress() { // 0 to 1
        return Math.min(elapsed / duration, 1f);
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isRunning() {
        return running;
    }
}