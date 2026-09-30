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
    private Sprite sprite;

    public Animation(Vector2f startPos, Vector2f endPos, Vector2f startScale, Vector2f endScale, int startRot, int endRot, Float duration, String easingType, Sprite sprite) {
        this.startPos = startPos;
        this.endPos = endPos;
        this.startScale = startScale;
        this.endScale = endScale;
        this.startRot = startRot;
        this.endRot = endRot;
        this.easingType = easingType;
        this.timer = new Timer(this.duration);
        this.sprite = sprite;

    }

    public void UpdateAnimation(float dt) {
        timer.update(dt);
        sprite.setTransformation(getPosition(),getScale(), getRotation());
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