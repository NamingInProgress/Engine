package com.vke.core.file.ogg.vorbis.data.floor;

import com.vke.core.file.ogg.vorbis.data.Fn;
import com.vke.core.file.ogg.vorbis.data.window.WindowData;
import com.vke.core.file.ogg.vorbis.header.setup.floor.Floor0;

public final class Floor0Data {
    public final Floor0 floor0Setup;
    public final boolean unused;
    public final float[] coefficients;
    public final int amplitude;

    public Floor0Data(Floor0 floor0Setup, boolean unused, float[] coefficients, int amplitude) {
        this.floor0Setup = floor0Setup;
        this.unused = unused;
        this.coefficients = coefficients;
        this.amplitude = amplitude;
    }

    public float[] compute(WindowData windowData) {
        int n = windowData.n;

        if (amplitude == 0) {
            return new float[n];
        }

        float[] map = new float[n];
        float[] output = new float[n];
        int floorRate = floor0Setup.f0_rate();
        int barkMapSize = floor0Setup.f0_barkMapSize();
        int floorOrder = floor0Setup.f0_order();
        int amplitudeBits = floor0Setup.f0_amplitudeBits();
        int amplitudeOffset = floor0Setup.f0_amplitudeOffset();

        for (int i = 0; i < n;) {
            float mapped = Fn.map(n, i, floorRate, barkMapSize);
            map[i] = mapped;
            double omega = Fn.omega(mapped, barkMapSize);
            double p, q;
            if (floorOrder % 2 != 0) {
                p = Fn.p_odd(omega, floorOrder, coefficients);
                q = Fn.q_odd(omega, floorOrder, coefficients);
            } else {
                p = Fn.p_even(omega, floorOrder, coefficients);
                q = Fn.q_even(omega, floorOrder, coefficients);
            }
            double linearFloor = Fn.linearFloor(amplitude, amplitudeBits, amplitudeOffset, p, q);
            float iterCond;
            do {
                iterCond = mapped;
                output[i] = (float) linearFloor;
                i++;
            } while (map[i] == iterCond);
        }

        return output;
    }
}
