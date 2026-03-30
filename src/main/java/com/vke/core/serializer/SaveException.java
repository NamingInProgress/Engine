package com.vke.core.serializer;

public class SaveException extends RuntimeException {
    public SaveException(String message) {
        super(message);
    }

    public SaveException(Throwable cause) {
        super(cause);
    }
}
