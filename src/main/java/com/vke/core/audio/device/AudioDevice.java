package com.vke.core.audio.device;

import com.vke.api.logger.Logger;
import com.vke.core.logger.LoggerFactory;
import com.vke.utils.io.Disposable;

public interface AudioDevice extends Disposable {
    Logger LOGGER = LoggerFactory.get("Audio");

    int read(float[] interleavedPCM, int amount);
}
