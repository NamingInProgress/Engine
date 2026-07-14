package com.vke.core.window;

import com.vke.api.utils.OSType;
import com.vke.api.window.Window;
import com.vke.api.window.WindowCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.framable.service.FramableManager;
import com.vke.core.window.callbacks.FramebufferCallbacks;
import com.vke.utils.Utils;
import com.vke.utils.console.AnsiColors;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.glfw.GLFW.*;

public class GlfwWindow implements Window {
    private static final String HERE = "Window Init";

    private final long window;
    private boolean minimized;
    private Size size;
    private final FramableManager framableManager;

    public GlfwWindow(VKEngine engine, WindowCreateInfo windowCreateInfo, FramableManager framableManager) throws IllegalStateException {
        this.framableManager = framableManager;

        if (Utils.getOSType() == OSType.LINUX) // TODO: Remove Later
            GLFW.glfwInitHint(GLFW.GLFW_PLATFORM, GLFW.GLFW_PLATFORM_X11); // thjis is for testing cuz linux and wayland wants to be funny
        if (!glfwInit()) {
            engine.getLogger().fatal("Failed to init GLFW!");
            throw new IllegalStateException("Failed to initialize GLFW!");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);

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

        setupCallbacks();
    }

    @Override
    public boolean isMinimized() {
        return minimized;
    }

    private void setupCallbacks() {
        glfwSetFramebufferSizeCallback(this.getHandle(), FramebufferCallbacks::onResize);
        glfwSetWindowIconifyCallback(this.getHandle(), FramebufferCallbacks::onMinimize);

        FramebufferCallbacks.minimize((state) -> minimized = state);
        FramebufferCallbacks.resize((w, h) -> size = new Size(w, h));
    }

    private void fetchSize() {
        int[] w = new int[1], h = new int[1];
        glfwGetFramebufferSize(this.getHandle(), w, h);
        size = new Size(w[0], h[0]);
    }

    @Override
    public void requestClose() {
        close();
    }

    @Override
    public Size getSize() {
        if (size == null) fetchSize();
        return size;
    }

    @Override
    public void show() {
        GLFW.glfwShowWindow(window);

        while (!GLFW.glfwWindowShouldClose(window)) {
            if (!isMinimized()) {
                framableManager.handlePossibleFrame();
            }

            GLFW.glfwPollEvents();
        }
    }

    public void close() {
        glfwSetWindowShouldClose(this.window, true);
        this.cleanUp();
    }

    private void cleanUp() {
        glfwDestroyWindow(this.window);
        glfwTerminate();
    }

    @Override
    public long getHandle() { return this.window; }

    public void disableCursor() {
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    @Override
    public String toString() { return "Window@" + glfwGetWindowTitle(this.window); }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof GlfwWindow)) return false;
        return this.getHandle() == ((GlfwWindow) other).getHandle();
    }

}
