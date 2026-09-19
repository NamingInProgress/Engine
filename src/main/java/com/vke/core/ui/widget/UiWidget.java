package com.vke.core.ui.widget;

import com.vke.core.ui.background.BackgroundLayer;
import com.vke.core.ui.composite.Compositor;

public abstract class UiWidget {
    protected final Compositor compositor;

    private BackgroundLayer backgroundLayer;

    private boolean layoutDirty;
    private boolean visualDirty;

    protected UiWidget(Compositor compositor) {
        this.compositor = compositor;
    }

    public void invalidate() {
        markLayoutDirty();
        markVisualDirty();
    }

    public void markLayoutDirty() {
        layoutDirty = true;
    }

    public void markVisualDirty() {
        visualDirty = true;
    }

    protected abstract boolean doesWidgetSupportCache();

    public boolean supportsCache() {
        return doesWidgetSupportCache() && backgroundLayer.supportsCache();
    }
}
