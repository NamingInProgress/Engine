package com.vke.core.input.mouse;

import com.vke.utils.collection.AbstractGlossary;

public class MousePositionState {
    private double x, y, lastX, lastY, dx, dy;
    private final Listener.Glossary listeners = new Listener.Glossary();

    void setX(double x) {
        this.x = x;
        this.listeners.onPositionChange(x, y);
    }

    void setY(double y) {
        this.y = y;
        this.listeners.onPositionChange(x, y);
    }

    void setXY(double x, double y) {
        this.x = x;
        this.y = y;
        this.listeners.onPositionChange(x, y);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getDy() {
        return dy;
    }

    public double getDx() {
        return dx;
    }

    public double getLastY() {
        return lastY;
    }

    public double getLastX() {
        return lastX;
    }

    public void listen(Listener listener) {
        this.listeners.addEntry(listener);
    }

    public void mute(Listener listener) {
        this.listeners.removeEntry(listener);
    }

    public void onFrame() {
        dx = x - lastX;
        dy = y - lastY;
        lastX = x;
        lastY = y;
    }

    public interface Listener {
        void onPositionChange(double x, double y);

        class Glossary extends AbstractGlossary<Listener> implements Listener {
            @Override
            public void onPositionChange(double x, double y) {
                entries.forEach(l -> l.onPositionChange(x, y));
            }
        }
    }
}