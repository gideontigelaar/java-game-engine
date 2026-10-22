package nl.team3.engine.core;

import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.util.Objects;

public record EngineConfig(String title,
                           int windowWidth, int windowHeight,
                           int minWindowWidth, int minWindowHeight,
                           int minGuiWidth, int minGuiHeight,
                           int guiScale,
                           Vector4fc clearColor,
                           boolean vsync) {

    public EngineConfig {
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(clearColor, "clearColor");
        if (windowWidth <= 0 || windowHeight <= 0 || minWindowWidth <= 0 || minWindowHeight <= 0
                || minGuiWidth <= 0 || minGuiHeight <= 0) {
            throw new IllegalArgumentException("Window and GUI sizes must be positive");
        }
        if (guiScale < 0) {
            throw new IllegalArgumentException("GUI scale must be 0 (auto) or higher");
        }
        clearColor = new Vector4f(clearColor);
    }

    public static Builder builder(String title) {
        return new Builder(title);
    }

    public static final class Builder {
        private final String title;
        private int windowWidth = 1280;
        private int windowHeight = 720;
        private int minWindowWidth = 640;
        private int minWindowHeight = 360;
        private int minGuiWidth = 1280;
        private int minGuiHeight = 720;
        private int guiScale = 0;
        private Vector4fc clearColor = new Vector4f(0f, 0f, 0f, 1f);
        private boolean vsync = true;

        private Builder(String title) {
            this.title = title;
        }

        public Builder windowSize(int width, int height) {
            this.windowWidth = width;
            this.windowHeight = height;
            return this;
        }

        public Builder minWindowSize(int width, int height) {
            this.minWindowWidth = width;
            this.minWindowHeight = height;
            return this;
        }

        // Smallest virtual size the GUI is designed for, automatic GUI scaling never goes below it
        public Builder minGuiSize(int width, int height) {
            this.minGuiWidth = width;
            this.minGuiHeight = height;
            return this;
        }

        // 0: auto, 1+: fixed scale (capped to what fits the window)
        public Builder guiScale(int guiScale) {
            this.guiScale = guiScale;
            return this;
        }

        public Builder clearColor(Vector4fc clearColor) {
            this.clearColor = clearColor;
            return this;
        }

        public Builder vsync(boolean vsync) {
            this.vsync = vsync;
            return this;
        }

        public EngineConfig build() {
            return new EngineConfig(title, windowWidth, windowHeight, minWindowWidth, minWindowHeight,
                    minGuiWidth, minGuiHeight, guiScale, clearColor, vsync);
        }
    }
}
