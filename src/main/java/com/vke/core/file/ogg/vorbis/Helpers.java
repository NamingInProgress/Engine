package com.vke.core.file.ogg.vorbis;

//https://xiph.org/vorbis/doc/Vorbis_I_spec.pdf#subsubsection.9.2.1
public class Helpers {
    public static int ilog(int x) {
        int ret = 0;
        while (x > 0) {
            ret++;
            x >>>= 1;
        }
        return ret;
    }

    public static float float32_unpack(int x) {
        int mantissa = x & 0x1fffff;
        int sign = x & 0x80000000;
        int exponent = (x & 0x7fe00000) >> 21;
        if (sign != 0) {
            mantissa = -mantissa;
        }
        return (float) (mantissa * Math.pow(2, exponent - 788));
    }

    public static int lookup1_values(int entries, int dimensions) {
        int v = (int) Math.floor(Math.pow(entries, 1.0 / dimensions));

        while (Math.pow(v + 1, dimensions) <= entries) {
            v++;
        }
        return v;
    }

    public static int low_neighbor(int[] v, int x) {
        int closestIdx = 0;
        int maxX = -1;
        for (int i = 0; i < x; i++) {
            if (v[i] < v[x] && v[i] > maxX) {
                maxX = v[i];
                closestIdx = i;
            }
        }
        return closestIdx;
    }

    public static int high_neighbor(int[] v, int x) {
        int closestIdx = 1;
        int minX = Integer.MAX_VALUE;
        for (int i = 0; i < x; i++) {
            if (v[i] > v[x] && v[i] < minX) {
                minX = v[i];
                closestIdx = i;
            }
        }
        return closestIdx;
    }

    public static int render_point(int x0, int y0, int x1, int y1, int X) {
        int dy = y1 - y0;
        int adx = x1 - x0;
        int ady = Math.abs(dy);
        int err = ady * (X - x0);
        int off = err / adx;
        if (dy < 0) {
            return y0 - off;
        }
        return y0 + off;
    }

    public static void render_line(int x0, int y0, int x1, int y1, float[] v) {
        int dy = y1 - y0;
        int adx = x1 - x0;
        int ady = Math.abs(dy);
        int base = dy / adx;
        int x = x0;
        int y = y0;
        int err = 0;

        int sy;
        if (dy < 0) {
            sy = base - 1;
        } else {
            sy = base + 1;
        }

        ady -= Math.abs(base) * adx;
        v[x] = y;
        for (int ix = x0 + 1; ix < x1; ix++) {
            err += ady;
            if (err >= adx) {
                err -= adx;
                y += sy;
            } else {
                y += base;
            }
            v[x] = y;
        }
    }
}
