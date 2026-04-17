package com.vke.core.geom;

public class Vec2 {
    public double x, y;

    public Vec2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static Vec2 ofInts(int[] ints) {
        return new Vec2(ints[0], ints[1]);
    }

    public double direction() {
        return Math.atan2(y, x);
    }

    public Vec2 perpendicular() {
        return new Vec2(-y, x);
    }

    public double length() {
        return Math.sqrt(x*x + y*y);
    }

    public double length2() {
        return x*x + y*y;
    }

    public Vec2 normalized() {
        double len = length();
        return new Vec2(x / len, y / len);
    }

    public double dot(Vec2 other) {
        return x * other.x + y * other.y;
    }

    public double cross2D(Vec2 other) {
        return x * other.y - y * other.x;
    }

    public int[] asInts() {
        return new int[] {(int) x, (int) y};
    }

    public Vec2 copy() {
        return new Vec2(x, y);
    }
}
