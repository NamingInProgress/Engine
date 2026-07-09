package com.vke.core.file.ogg.vorbis.data.window;

public class WindowData {
    public final float[] window;
    public final int n;

    public WindowData(float[] window, int n) {
        this.window = window;
        this.n = n;
    }
}
