package nl.team3.engine.graphics.animation;

public final class Interpolators {
    private Interpolators() {
    }

    public static float ease(float start, float end, float t, EasingType type) {
        float ease;
        switch (type) {
            case LINEAR:
                return start + (end - start) * t;

            case EXPONENTIAL_IN:
                if (t == 0f) return start;
                ease = (float) Math.pow(2, 10 * (t - 1));
                return start + (end - start) * ease;

            case EXPONENTIAL_OUT:
                if (t == 0f) return start;
                if (t >= 1f) return end;
                ease = 1f -(float) Math.pow(2, -10 * (t));
                return start + (end - start) * ease;

            case EXPONENTIAL_IN_OUT:
                if (t <= 0f) return start;
                if (t >= 1f) return end;

                ease = t < 0.5f
                        ? 0.5f * (float) Math.pow(2, 20 * t - 10)
                        : 1f - 0.5f * (float) Math.pow(2, -20 * t + 10);

                return start + (end - start) * ease;
        }
        return start + (end - start) * t;
    }
}