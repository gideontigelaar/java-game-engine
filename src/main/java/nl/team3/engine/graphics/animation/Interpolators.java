package nl.team3.engine.graphics.animation;

public class Interpolators {
    public static float Easing(float start, float end, float t, String type) {
        float ease;
        switch (type) {
            case "Linear":
                return start + (end - start) * t;

            case "ExponentialIn":

                if (t == 0f) return start;
                ease = (float) Math.pow(2, 10 * (t - 1));
                return start + (end - start) * ease;

            case "ExponentialOut":

                if (t == 0f) return start;
                ease = 1f -(float) Math.pow(2, -10 * (t));
                return start + (end - start) * ease;


            case "ExponentiaInOut":
                if (t <= 0f) return start;
                if (t >= 1f) return end;

                ease = t < 0.5f
                        ? 0.5f * (float) Math.pow(2, 20 * t - 10)
                        : 1f - 0.5f * (float) Math.pow(2, -20 * t + 10);

                return start + (end - start) * ease;
        }



        //if string cant be compared to anything in the switch
        return start + (end - start) * t;

        }
}
