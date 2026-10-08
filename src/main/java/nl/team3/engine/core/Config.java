package nl.team3.engine.core;

import org.joml.Vector4f;

public class Config {
    // Window
    public static final String WINDOW_TITLE = "Game Engine";
    public static final int WINDOW_WIDTH = 1920;
    public static final int WINDOW_HEIGHT = 1080;
    public static final int MIN_WINDOW_WIDTH = 960;
    public static final int MIN_WINDOW_HEIGHT = 540;
    public static final int GUI_SCALE_OVERRIDE = 0; // 0: auto, 1+: fixed scale (capped to what fits the window)
    public static final int MIN_GUI_WIDTH = 1280;
    public static final int MIN_GUI_HEIGHT = 720;
    public static final Vector4f BG_COLOR = new Vector4f(0.62f, 0.62f, 0.62f, 1.0f);
    public static final boolean VSYNC_ENABLED = true;

    // Engine
}