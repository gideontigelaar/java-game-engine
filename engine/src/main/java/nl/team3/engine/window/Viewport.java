package nl.team3.engine.window;

import static org.lwjgl.opengl.GL11.glViewport;

public class Viewport {
    private final int minWindowWidth;
    private final int minWindowHeight;
    private final int minGuiWidth;
    private final int minGuiHeight;

    private int windowWidth = 1;
    private int windowHeight = 1;
    private int framebufferWidth = 1;
    private int framebufferHeight = 1;

    private int requestedGuiScale;
    private int guiScale = 1;
    private int scaledWidth;
    private int scaledHeight;

    // guiScale 0: auto, 1+: fixed scale (capped to what fits the window)
    public Viewport(int minWindowWidth, int minWindowHeight, int minGuiWidth, int minGuiHeight, int guiScale) {
        this.minWindowWidth = minWindowWidth;
        this.minWindowHeight = minWindowHeight;
        this.minGuiWidth = minGuiWidth;
        this.minGuiHeight = minGuiHeight;
        this.requestedGuiScale = guiScale;
        this.scaledWidth = minGuiWidth;
        this.scaledHeight = minGuiHeight;
    }

    public void update(int windowWidth, int windowHeight, int framebufferWidth, int framebufferHeight) {
        if (windowWidth <= 0 || windowHeight <= 0 || framebufferWidth <= 0 || framebufferHeight <= 0) {
            return;
        }

        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.framebufferWidth = framebufferWidth;
        this.framebufferHeight = framebufferHeight;

        recalculate();
    }

    public void setGuiScale(int requestedGuiScale) {
        this.requestedGuiScale = requestedGuiScale;
        recalculate();
    }

    private void recalculate() {
        if (requestedGuiScale <= 0) {
            guiScale = Math.max(1, Math.min(
                    framebufferWidth / minGuiWidth,
                    framebufferHeight / minGuiHeight));
        } else {
            int maxScale = Math.max(1, Math.min(
                    framebufferWidth / minWindowWidth,
                    framebufferHeight / minWindowHeight));
            guiScale = Math.min(requestedGuiScale, maxScale);
        }

        scaledWidth = (int) Math.ceil(framebufferWidth / (double) guiScale);
        scaledHeight = (int) Math.ceil(framebufferHeight / (double) guiScale);
    }

    public void apply() {
        glViewport(0, 0, framebufferWidth, framebufferHeight);
    }

    public double toVirtualX(double windowX) {
        return windowX * framebufferWidth / windowWidth / guiScale;
    }

    public double toVirtualY(double windowY) {
        return windowY * framebufferHeight / windowHeight / guiScale;
    }

    public double deltaToVirtualX(double windowDeltaX) {
        return toVirtualX(windowDeltaX);
    }

    public double deltaToVirtualY(double windowDeltaY) {
        return toVirtualY(windowDeltaY);
    }

    public int getGuiScale() { return guiScale; }
    public int getScaledWidth() { return scaledWidth; }
    public int getScaledHeight() { return scaledHeight; }
}