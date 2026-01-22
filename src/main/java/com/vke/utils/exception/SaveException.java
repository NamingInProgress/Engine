package com.vke.utils.exception;

public class SaveException extends RuntimeException {
    public SaveException(String message) {
        super(message);
    }

    public SaveException(Throwable cause) {
        super(cause);
    }
}
