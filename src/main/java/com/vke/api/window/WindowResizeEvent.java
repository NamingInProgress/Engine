package com.vke.api.window;

import com.vke.api.event.CancellableEvent;
import com.vke.api.event.Event;

public class WindowResizeEvent extends Event {
    public final Window window;
    public final int width, height;

    public WindowResizeEvent(Window window, int width, int height) {
        this.window = window;
        this.width = width;
        this.height = height;
    }
}
