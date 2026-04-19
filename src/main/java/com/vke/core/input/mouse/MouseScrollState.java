package com.vke.core.input.mouse;

import com.vke.utils.collection.AbstractGlossary;

public class MouseScrollState {
    private double dx, dy;
    private ScrollDirection direction = ScrollDirection.Undecypherable;
    private boolean dirty = false;
    private Listener.Glossary listeners = new Listener.Glossary();

    public double getDx() {
        return dx;
    }

    public double getDy() {
        return dy;
    }

    public ScrollDirection getDirection() {
        return direction;
    }

    void setDxDy(double dx, double dy) {
        this.dx = dx;
        this.dy = dy;
        this.direction = ScrollDirection.fromDelta(dx, dy);
        this.dirty = true;
        listeners.onScroll(dx, dy, direction);
    }

    void onFrame() {
        if (dirty) {
            dirty = false;
            return;
        }

        this.dx = 0;
        this.dy = 0;
        this.direction = ScrollDirection.Undecypherable;
    }

    public void listen(Listener listener) {
        this.listeners.addEntry(listener);
    }

    public void mute(Listener listener) {
        this.listeners.removeEntry(listener);
    }

    public interface Listener {
        void onScroll(double dx, double dy, ScrollDirection direction);

        class Glossary extends AbstractGlossary<Listener> implements Listener {
            @Override
            public void onScroll(double x, double y, ScrollDirection dir) {
                entries.forEach(l -> l.onScroll(x, y, dir));
            }
        }
    }
}