package com.vke.core.ui.background;

public abstract class BackgroundLayer {
    public abstract boolean supportsCache();

    public abstract void draw();
}
