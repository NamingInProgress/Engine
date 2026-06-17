package com.vke.core.file.ogg.vorbis.setup;

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
}
