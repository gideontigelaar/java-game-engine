package nl.team3.engine.graphics.animation;

import java.util.List;

public class AnimationSequence {
    private final List<Animation> animationList;
    private int currentAnimation = 0;
    private boolean running = false;

    public AnimationSequence(List<Animation> animationList) {
        this.animationList = animationList;
    }

    public void startSequence() {
        if (animationList == null || animationList.isEmpty()) {
            return;
        }
        currentAnimation = 0;
        animationList.get(currentAnimation).startAnimation();
        running = true;
    }

    public void update(float dt) {
        if (!running || animationList == null || animationList.isEmpty()) {
            return;
        }

        Animation current = animationList.get(currentAnimation);
        current.UpdateAnimation(dt);

        if (current.getTimer().isFinished()) {
            if (currentAnimation + 1 < animationList.size()) {
                currentAnimation++;
                animationList.get(currentAnimation).startAnimation();
            } else {
                running = false;
            }
        }
    }

    public boolean isRunning() {
        return running;
    }
}