package com.vke.core.file.jpeg;

public class JpegHelper {
    public static int[] RGBtoYCbCr(int r, int g, int b) {
        int[] out = new int[3];
        out[0] = (int) (0.299 * r + 0.587 * g + 0.114 * b);
        out[1] = (int) (-0.1687 * r + 0.3313 * g + 0.5 * b + 128);
        out[2] = (int) (0.5 * r - 0.4187 * g - 0.0813 * b + 128);
        return out;
    }

    public static int[] YCbCrtoRGB(int y, int cb, int cr) {
        int[] out = new int[3];
        out[0] = (int) (y + 1.402 * (cr - 128));
        out[1] = (int) (y - 0.34414 * (cb - 128) - 0.71414 * (cr - 128));
        out[2] = (int) (y + 1.772 * (cb - 128));
        return out;
    }
}
