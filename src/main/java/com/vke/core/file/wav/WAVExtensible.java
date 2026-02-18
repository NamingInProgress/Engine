package com.vke.core.file.wav;

import java.util.UUID;

public final class WAVExtensible {
    private final int validBitsPerSample;
    private final long channelMask;
    private final UUID subFormat;

    public WAVExtensible(int validBitsPerSample, long channelMask, UUID subFormat) {
        this.validBitsPerSample = validBitsPerSample;
        this.channelMask = channelMask;
        this.subFormat = subFormat;
    }

    public int getValidBitsPerSample() {
        return validBitsPerSample;
    }

    public long getChannelMask() {
        return channelMask;
    }

    public UUID getSubFormat() {
        return subFormat;
    }
}
