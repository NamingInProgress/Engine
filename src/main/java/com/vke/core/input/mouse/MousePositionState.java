package com.vke.core.input.mouse;

import com.vke.utils.collection.AbstractGlossary;

public class MousePositionState {
    private int x, y;
    private final Listener.Glossary listeners = new Listener.Glossary();

    void setX(int x) {
        this.x = x;
        this.listeners.onPositionChange(x, y);
    }

    void setY(int y) {
        this.y = y;
        this.listeners.onPositionChange(x, y);
    }

    void setXY(int x, int y) {
        this.x = x;
        this.y = y;
        this.listeners.onPositionChange(x, y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void listen(Listener listener) {
        this.listeners.addEntry(listener);
    }

    public void mute(Listener listener) {
        this.listeners.removeEntry(listener);
    }

    public interface Listener {
        void onPositionChange(int x, int y);

        class Glossary extends AbstractGlossary<Listener> implements Listener {
            @Override
            public void onPositionChange(int x, int y) {
                entries.forEach(l -> l.onPositionChange(x, y));
            }
        }
    }
}