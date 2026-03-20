package com.vke.core.serializer;

public class LoadException extends RuntimeException {
    public LoadException(String message) {
        super(message);
    }

    public LoadException(Throwable cause) {
        super(cause);
    }
}
