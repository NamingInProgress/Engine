package com.vke.core.audio.device;

import com.vke.utils.io.Disposable;

public interface AudioDevice extends Disposable {
    int read(float[] interleavedPCM, int amount);
}
