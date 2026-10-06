package nl.team3.engine.graphics.animation;

import java.util.List;

public class AnimationSequence {
    private List<Animation> animationList;
    private int currentAnimation = 0;
    public boolean running = false;
    public AnimationSequence(List<Animation> animationList){
        this.animationList = animationList;
    }
    public void startSequence(){
        currentAnimation = 0;
        animationList.get(currentAnimation).startAnimation();
        running = true;
    }
    public void Update(float dt){
        if (!running) return;

        Animation current = animationList.get(currentAnimation);
        current.UpdateAnimation(dt);

        if (current.getTimer().getRemaining() <= 0) {
            if (currentAnimation + 1 < animationList.size()) {
                currentAnimation++;
                animationList.get(currentAnimation).startAnimation();
            } else {
                running = false;
            }
        }
    }
}