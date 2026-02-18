package com.vke.core.file.wav;

public enum WAVAudioFormat {
    PCM(0x0001),
    IEEE_FLOAT(0x0003),
    ALAW(0x0006),
    MULAW(0x0007),
    EXTENSIBLE(0xFFFE),
    UNKNOWN(-1);

    private final int code;

    WAVAudioFormat(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static WAVAudioFormat fromCode(int code) {
        for (WAVAudioFormat f : values()) {
            if (f.code == code) return f;
        }
        return UNKNOWN;
    }
}
