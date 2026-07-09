package com.vke.core.file.ogg.vorbis.data;

import static java.lang.Math.*;

public class Fn {
    private static double sq(double x) {
        return x * x;
    }

    private static double sin2(double x) {
        double s = sin(x);
        return s * s;
    }

    private static double cos2(double x) {
        double c = cos(x);
        return c * c;
    }

    private static double dv(double a, double b) {
        return a / b;
    }

    @FunctionalInterface
    private interface Fi {
        double f(int i);
    }

    private static double prod(int limit, int start, Fi fn) {
        double base = 1.0;
        for (int i = start; i <= limit; i++) {
            base *= fn.f(i);
        }
        return base;
    }

    //WINDOW

    public static float windowLeftSlope(int i, long start, long n) {
        double inner = ((i - start + 0.5) / n) * (PI / 2.0);
        return (float) sin((PI / 2.0) * sin2(inner));
    }

    public static float windowRightSlope(int i, long right, long n) {
        double inner = (((i - right + 0.5) / n) * (PI / 2.0)) + (PI / 2.0);
        return (float) sin((PI / 2.0) * sin2(inner));
    }

    //FLOOR0

    public static double bark(double x) {
        return 13.1 * atan(0.00074 * x) + 2.24 * atan(0.0000000185 * sq(x)) + 0.0001 * x;
    }

    public static double foobar(int n, int i, int floorRate, int barkMapSize) {
        return floor(bark(dv(floorRate * i, 2 * n)) * dv(barkMapSize, bark(0.5 * floorRate)));
    }

    public static float map(int n, int i, int floorRate, int barkMapSize) {
        if (i == n) return -1;
        return (float) min(barkMapSize - 1, foobar(n, i, floorRate, barkMapSize));
    }

    public static double omega(float[] map, int i, int barkMapSize) {
        return (float) dv(PI * map[i], barkMapSize);
    }

    public static double p_odd(double omega, int floorOrder, float[] coeff) {
        double p = 1 - cos2(omega);
        int pLim = (int) dv(floorOrder - 3, 2);
        return p * prod(pLim, 0, j -> 4 * sq(cos(coeff[2 * j + 1]) - cos(omega)));

    }

    public static double q_odd(double omega, int floorOrder, float[] coeff) {
        double q = 0.25;
        int qLim = (int) dv(floorOrder - 1, 2);
        return q * prod(qLim, 0, j -> 4 * sq(cos(coeff[2 * j]) - cos(omega)));
    }

    public static double p_even(double omega, int floorOrder, float[] coeff) {
        double p = dv(1 - cos2(omega), 2);
        int pLim = (int) dv(floorOrder - 2, 2);
        return p * prod(pLim, 0, j -> 4 * sq(cos(coeff[2 * j + 1]) - cos(omega)));

    }

    public static double q_even(double omega, int floorOrder, float[] coeff) {
        double q = dv(1 + cos2(omega), 2);
        int qLim = (int) dv(floorOrder - 2, 2);
        return q * prod(qLim, 0, j -> 4 * sq(cos(coeff[2 * j]) - cos(omega)));
    }
}
