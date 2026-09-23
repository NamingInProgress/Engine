package com.vke.api.window;

import org.joml.Vector2i;
import org.lwjgl.PointerBuffer;

public interface Window {
    long getHandle();

    /**
     * Presents the window on the screen and also starts up the main loop.
     * Blocks the Thread until window closes.
     */
    void show();
    void requestClose();

    void showCursor();
    void hideCursor();

    OpenWindowState getOpenState();
    void setOpenState(OpenWindowState state);

    Size getSize();

    default void setSize(Size size) {
        setSize(size.width, size.height);
    }

    void setSize(int width, int height);
    void setWidth(int width);
    void setHeight(int height);

    void setX(int x);
    void setY(int y);

    default void setPosition(Vector2i xy) {
        setPosition(xy.x, xy.y);
    }

    void setPosition(int x, int y);
    Vector2i getPosition();

    void setTitle(CharSequence title);
    CharSequence getTitle();

    void setRezizable(boolean resizable);
    boolean getResizable();

    void setDecorated(boolean decorated);
    boolean getDecorated();

    void setVSync(boolean vsync);
    boolean getVSync();


    record Size(int width, int height) {}
    enum OpenWindowState {
        Minimized,
        Maximized,
        Normal, Fullscreen
    }
}
