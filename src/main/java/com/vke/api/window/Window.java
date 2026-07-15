package com.vke.api.window;

public interface Window {

    long getHandle();
    boolean isMinimized();
    void show();
    void requestClose();

    void disableCursor();

    Size getSize();
    record Size(int width, int height) {}
}
