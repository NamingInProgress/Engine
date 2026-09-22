package com.vke.core.ui.widget;

import com.vke.core.ui.composite.Compositor;

import java.util.LinkedList;

public abstract class UiParent extends UiWidget {
    private final LinkedList<UiWidget> children;

    protected UiParent(Compositor compositor) {
        super(compositor);

        this.children = new LinkedList<>();
    }

    public void addChild(UiWidget child) {
        children.add(child);
        markLayoutDirty();
    }

    public void insertChild(int index, UiWidget child) {
        children.add(index, child);
        markLayoutDirty();
    }

    public UiWidget removeChildAt(int index) {
        markLayoutDirty();
        UiWidget widget = children.remove(index);
        compositor.removeWidget(widget);
        return widget;
    }

    public UiWidget removeChild(UiWidget child) {
        markLayoutDirty();
        if (children.remove(child)) {
            compositor.removeWidget(child);
            return child;
        }
        return null;
    }
}
