package com.vke.core.file.ogg.vorbis.data.floor;

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
        for (int i = 0; i < n; i++) {

        }
    }


}
