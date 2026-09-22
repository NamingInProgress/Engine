package com.vke.core.window;

import com.vke.api.event.EventBus;
import com.vke.api.utils.OSType;
import com.vke.api.window.Window;
import com.vke.api.window.WindowCreateInfo;
import com.vke.api.window.WindowResizeEvent;
import com.vke.core.VKEngine;
import com.vke.core.framable.service.FramableManager;
import com.vke.core.services2.Services;
import com.vke.core.window.callbacks.FramebufferCallbacks;
import com.vke.utils.Utils;
import com.vke.utils.exception.Unreachable;
import org.joml.Vector2i;
import org.lwjgl.glfw.*;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.glfw.GLFW.*;

public class GlfwWindow implements Window {
    private static final String HERE = "Window Init";

    private final long window;
    private Size size;
    private final Vector2i pos;
    private CharSequence title;
    private final FramableManager framableManager;
    private final EventBus bus;

    private OpenWindowState state = OpenWindowState.Normal;

    private int preFullscreenX, preFullscreenY, preFullscreenWidth, preFullscreenHeight;
    private boolean isResizable;
    private boolean isDecorated;
    private boolean vsync;

    private GLFWWindowPosCallback windowPosCallback;
    private GLFWWindowIconifyCallback windowIconifyCallback;
    private GLFWWindowMaximizeCallback windowMaximizeCallback;

    public GlfwWindow(VKEngine engine, WindowCreateInfo windowCreateInfo, FramableManager framableManager) throws IllegalStateException {
        this.framableManager = framableManager;
        this.bus = engine.service(Services.EVENT_BUS);

        if (Utils.getOSType() == OSType.LINUX) // TODO: Remove Later
            GLFW.glfwInitHint(GLFW.GLFW_PLATFORM, GLFW.GLFW_PLATFORM_X11);

        if (!glfwInit()) {
            engine.getLogger().fatal("Failed to init GLFW!");
            throw new IllegalStateException("Failed to initialize GLFW!");
        }

        this.isResizable = windowCreateInfo.resizable;
        this.isDecorated = windowCreateInfo.decorated;

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

        this.title = windowCreateInfo.title;

        int[] pWidth = new int[1];
        int[] pHeight = new int[1];
        glfwGetWindowSize(window, pWidth, pHeight);

        GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

        if (vidMode == null) {
            engine.throwException(new IllegalStateException("Failed to get Video Mode for primary monitor!"), HERE);
            throw new Unreachable();
        }

        pos = new Vector2i(
                (vidMode.width() - pWidth[0]) / 2,
                (vidMode.height() - pHeight[0]) / 2
        );

        setPosition(pos.x, pos.y);

        setupCallbacks();

        if (windowCreateInfo.fullscreen) {
            setOpenState(OpenWindowState.Fullscreen);
        }
    }

    private void setupCallbacks() {
        glfwSetFramebufferSizeCallback(this.window, FramebufferCallbacks::onResize);

        windowPosCallback = glfwSetWindowPosCallback(window, (win, xpos, ypos) -> {
            pos.x = xpos;
            pos.y = ypos;
        });

        windowIconifyCallback = glfwSetWindowIconifyCallback(window, (win, iconified) -> {
            if (iconified) {
                state = OpenWindowState.Minimized;
            } else {
                state = glfwGetWindowAttrib(win, GLFW_MAXIMIZED) == GLFW_TRUE
                        ? OpenWindowState.Maximized
                        : OpenWindowState.Normal;
            }
        });

        windowMaximizeCallback = glfwSetWindowMaximizeCallback(window, (win, maximized) -> {
            if (maximized) {
                state = OpenWindowState.Maximized;
            } else if (state != OpenWindowState.Minimized) {
                state = OpenWindowState.Normal;
            }
        });

        FramebufferCallbacks.resize((w, h) -> size = new Size(w, h));
        FramebufferCallbacks.resize((w, h) -> bus.fire(new WindowResizeEvent(this, w, h)));
    }

    private void fetchSize() {
        int[] w = new int[1], h = new int[1];
        glfwGetFramebufferSize(this.window, w, h);
        size = new Size(w[0], h[0]);
    }

    @Override
    public void requestClose() {
        close();
    }

    @Override
    public void showCursor() {
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }

    @Override
    public void hideCursor() {
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    @Override
    public Size getSize() {
        if (size == null) fetchSize();
        return size;
    }

    @Override
    public void setSize(int width, int height) {
        glfwSetWindowSize(window, width, height);
    }

    @Override
    public void setWidth(int width) {
        glfwSetWindowSize(window, width, getSize().height());
    }

    @Override
    public void setHeight(int height) {
        glfwSetWindowSize(window, getSize().width(), height);
    }

    @Override
    public void setX(int x) {
        glfwSetWindowPos(window, x, pos.y);
    }

    @Override
    public void setY(int y) {
        glfwSetWindowPos(window, pos.x, y);
    }

    @Override
    public void setPosition(int x, int y) {
        glfwSetWindowPos(window, x, y);
    }

    public void setPosition(Vector2i position) {
        setPosition(position.x, position.y);
    }

    @Override
    public Vector2i getPosition() {
        return pos;
    }

    @Override
    public void setTitle(CharSequence title) {
        this.title = title;
        glfwSetWindowTitle(window, title);
    }

    @Override
    public CharSequence getTitle() {
        return title;
    }

    @Override
    public void setRezizable(boolean resizable) {
        this.isResizable = resizable;
        glfwSetWindowAttrib(window, GLFW_RESIZABLE, resizable ? GLFW_TRUE : GLFW_FALSE);
    }

    @Override
    public boolean getResizable() {
        return isResizable;
    }

    @Override
    public void setDecorated(boolean decorated) {
        this.isDecorated = decorated;
        glfwSetWindowAttrib(window, GLFW_DECORATED, decorated ? GLFW_TRUE : GLFW_FALSE);
    }

    @Override
    public boolean getDecorated() {
        return isDecorated;
    }

    @Override
    public void setVSync(boolean vsync) {
        this.vsync = vsync;
        glfwSwapInterval(vsync ? 1 : 0);
    }

    @Override
    public boolean getVSync() {
        return vsync;
    }

    @Override
    public void show() {
        glfwShowWindow(window);

        while (!glfwWindowShouldClose(window)) {
            if (state != OpenWindowState.Minimized) {
                framableManager.handlePossibleFrame();
            }

            glfwPollEvents();
        }

        this.cleanUp();
    }

    public void close() {
        glfwSetWindowShouldClose(this.window, true);
    }

    private void cleanUp() {
        if (windowPosCallback != null) windowPosCallback.free();
        if (windowIconifyCallback != null) windowIconifyCallback.free();
        if (windowMaximizeCallback != null) windowMaximizeCallback.free();

        glfwDestroyWindow(this.window);
        glfwTerminate();
    }

    @Override
    public long getHandle() { return this.window; }

    @Override
    public OpenWindowState getOpenState() {
        return state;
    }

    @Override
    public void setOpenState(OpenWindowState newState) {
        if (this.state == newState) return;

        if (this.state == OpenWindowState.Fullscreen) {
            glfwSetWindowMonitor(window, MemoryUtil.NULL, preFullscreenX, preFullscreenY, preFullscreenWidth, preFullscreenHeight, GLFW_DONT_CARE);
        }

        switch (newState) {
            case Normal -> glfwRestoreWindow(window);
            case Minimized -> glfwIconifyWindow(window);
            case Maximized -> glfwMaximizeWindow(window);
            case Fullscreen -> {
                long primaryMonitor = glfwGetPrimaryMonitor();
                if (primaryMonitor != MemoryUtil.NULL) {
                    GLFWVidMode vidMode = glfwGetVideoMode(primaryMonitor);
                    if (vidMode != null) {
                        this.preFullscreenX = pos.x;
                        this.preFullscreenY = pos.y;
                        this.preFullscreenWidth = getSize().width();
                        this.preFullscreenHeight = getSize().height();

                        glfwSetWindowMonitor(window, primaryMonitor, 0, 0, vidMode.width(), vidMode.height(), vidMode.refreshRate());
                    }
                }
            }
        }
        this.state = newState;
    }

    @Override
    public String toString() { return "Window@" + glfwGetWindowTitle(this.window); }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof GlfwWindow)) return false;
        return this.getHandle() == ((GlfwWindow) other).getHandle();
    }
}