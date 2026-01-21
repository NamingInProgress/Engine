package com.vke.api.window;

public class WindowCreateInfo {
    private final static int DEFAULT_WIDTH = 800, DEFAULT_HEIGHT = 600;
    private final static String DEFAULT_TITLE = "Hello VKEngine!";

    public String title;
    public int width, height;
    public boolean decorated;
    public boolean resizable;
    public boolean fullscreen;

    public WindowCreateInfo(int width, int height, String title) {
        this.width = width;
        this.height = height;
        this.title = title;
        this.decorated = true;
        this.resizable = true;
        this.fullscreen = false;
    }

    public WindowCreateInfo() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_TITLE);
    }

    public WindowCreateInfo(int width, int height) {
        this(width, height, DEFAULT_TITLE);
    }

    public WindowCreateInfo(String title) {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT, title);
    }

    public int isDecorated() {
        return decorated ? 1 : 0;
    }

    public int isResizable() {
        return resizable ? 1 : 0;
    }

    public int isFullscreen() {
        return fullscreen ? 1 : 0;
    }
}
