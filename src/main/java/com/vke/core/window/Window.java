package com.vke.core.window;

import com.vke.api.window.WindowCreateInfo;
import com.vke.core.VKEngine;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.glfw.GLFW.*;

public class Window {
    private static final String HERE = "Window Init";

    private final long window;

    public Window(VKEngine engine, WindowCreateInfo windowCreateInfo) throws IllegalStateException {
        if (!glfwInit()) {
            engine.getLogger().fatal("Failed to init GLFW!");
            throw new IllegalStateException("Failed to initialize GLFW!");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);

        glfwWindowHint(GLFW_RESIZABLE, windowCreateInfo.isResizable());
        glfwWindowHint(GLFW_SOFT_FULLSCREEN, windowCreateInfo.isFullscreen());
        glfwWindowHint(GLFW_DECORATED, windowCreateInfo.isDecorated());

        window = glfwCreateWindow(windowCreateInfo.width, windowCreateInfo.height, windowCreateInfo.title, MemoryUtil.NULL, MemoryUtil.NULL);

        if (window == MemoryUtil.NULL) {
            engine.throwException(new IllegalStateException("Failed to create window handle!"), HERE);
        }

        int[] pWidth = new int[1];
        int[] pHeight = new int[1];
        glfwGetWindowSize(window, pWidth, pHeight);

        GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

        if (vidMode == null) {
            engine.throwException(new IllegalStateException("Failed to get Video Mode for primary monitor!"), HERE);
        }

        glfwSetWindowPos(
                window,
                (vidMode.width() - pWidth[0]) / 2,
                (vidMode.height() - pHeight[0]) / 2
        );
    }

    public void close() {
        glfwSetWindowShouldClose(this.window, true);
        this.cleanUp();
    }

    private void cleanUp() {
        glfwDestroyWindow(this.window);
        glfwTerminate();
    }

    public long getHandle() { return this.window; }

    @Override
    public String toString() { return "Window@" + glfwGetWindowTitle(this.window); }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Window)) return false;
        return this.getHandle() == ((Window) other).getHandle();
    }

}
