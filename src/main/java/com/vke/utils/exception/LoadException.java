package com.vke.utils.exception;

public class LoadException extends RuntimeException {
    public LoadException(String message) {
        super(message);
    }

    public LoadException(Throwable cause) {
        super(cause);
    }
}
