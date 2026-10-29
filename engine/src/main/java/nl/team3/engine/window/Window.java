package nl.team3.engine.window;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

public final class Window {
    private final long handle;

    public Window(String title, int width, int height, int minWidth, int minHeight, boolean vsync) {
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        handle = GLFW.glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL);
        if (handle == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create GLFW window");
        }
        fitAndCenter(width, height);
        GLFW.glfwSetWindowSizeLimits(handle, minWidth, minHeight, GLFW.GLFW_DONT_CARE, GLFW.GLFW_DONT_CARE);

        GLFW.glfwMakeContextCurrent(handle);
        GLFW.glfwSwapInterval(vsync ? 1 : 0);
        GLFW.glfwShowWindow(handle);
    }

    public long getHandle() {
        return handle;
    }

    public boolean shouldClose() {
        return GLFW.glfwWindowShouldClose(handle);
    }

    public void requestClose() {
        GLFW.glfwSetWindowShouldClose(handle, true);
    }

    public void swapBuffers() {
        GLFW.glfwSwapBuffers(handle);
    }

    public void setCursorVisible(boolean visible) {
        GLFW.glfwSetInputMode(handle, GLFW.GLFW_CURSOR, visible ? GLFW.GLFW_CURSOR_NORMAL : GLFW.GLFW_CURSOR_HIDDEN);
    }

    public void setCursorMode(int glfwCursorMode) {
        GLFW.glfwSetInputMode(handle, GLFW.GLFW_CURSOR, glfwCursorMode);
    }

    public void onFramebufferResize(Runnable listener) {
        GLFW.glfwSetFramebufferSizeCallback(handle, (win, width, height) -> listener.run());
    }

    public void updateViewport(Viewport viewport) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer winW = stack.mallocInt(1);
            IntBuffer winH = stack.mallocInt(1);
            IntBuffer fbW = stack.mallocInt(1);
            IntBuffer fbH = stack.mallocInt(1);
            GLFW.glfwGetWindowSize(handle, winW, winH);
            GLFW.glfwGetFramebufferSize(handle, fbW, fbH);
            viewport.update(winW.get(0), winH.get(0), fbW.get(0), fbH.get(0));
        }
    }

    public void destroy() {
        GLFW.glfwDestroyWindow(handle);
    }

    private void fitAndCenter(int targetWidth, int targetHeight) {
        long monitor = GLFW.glfwGetPrimaryMonitor();
        if (monitor == MemoryUtil.NULL) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer areaX = stack.mallocInt(1);
            IntBuffer areaY = stack.mallocInt(1);
            IntBuffer areaW = stack.mallocInt(1);
            IntBuffer areaH = stack.mallocInt(1);
            GLFW.glfwGetMonitorWorkarea(monitor, areaX, areaY, areaW, areaH);

            IntBuffer left = stack.mallocInt(1);
            IntBuffer top = stack.mallocInt(1);
            IntBuffer right = stack.mallocInt(1);
            IntBuffer bottom = stack.mallocInt(1);
            GLFW.glfwGetWindowFrameSize(handle, left, top, right, bottom);

            int frameW = left.get(0) + right.get(0);
            int frameH = top.get(0) + bottom.get(0);

            float fit = Math.min(1f, Math.min(
                    (areaW.get(0) - frameW) / (float) targetWidth,
                    (areaH.get(0) - frameH) / (float) targetHeight));
            int width = Math.max(1, Math.round(targetWidth * fit));
            int height = Math.max(1, Math.round(targetHeight * fit));
            GLFW.glfwSetWindowSize(handle, width, height);

            int posX = areaX.get(0) + (areaW.get(0) - frameW - width) / 2 + left.get(0);
            int posY = areaY.get(0) + (areaH.get(0) - frameH - height) / 2 + top.get(0);
            GLFW.glfwSetWindowPos(handle, posX, posY);
        }
    }
}
