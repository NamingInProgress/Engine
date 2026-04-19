package com.vke.core.input.mouse;

public enum ScrollDirection {
    Undecypherable,
    Up,
    Down,
    Left,
    Right;

    public static ScrollDirection fromDelta(double dx, double dy) {
        if (dx == 0 && dy == 0) {
            return ScrollDirection.Undecypherable;
        }

        if (Math.abs(dx) >= Math.abs(dy)) {
            return dx > 0 ? ScrollDirection.Left : ScrollDirection.Right;
        } else {
            return dy > 0 ? ScrollDirection.Up : ScrollDirection.Down;
        }
    }
}
