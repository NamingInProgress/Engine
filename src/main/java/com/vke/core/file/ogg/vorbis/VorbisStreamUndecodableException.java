package com.vke.core.file.ogg.vorbis;

import java.io.IOException;

public class VorbisStreamUndecodableException extends IOException {
    public VorbisStreamUndecodableException() {
        super();
    }

    public VorbisStreamUndecodableException(String message) {
        super(message);
    }

    public VorbisStreamUndecodableException(String message, Throwable cause) {
        super(message, cause);
    }

    public VorbisStreamUndecodableException(Throwable cause) {
        super(cause);
    }
}
