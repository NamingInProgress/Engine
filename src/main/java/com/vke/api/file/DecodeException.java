package com.vke.api.file;

import java.io.IOException;

public class DecodeException extends Exception {
    public DecodeException() {
    }

    public DecodeException(String message) {
        super(message);
    }

    public DecodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public DecodeException(Throwable cause) {
        super(cause);
    }

    public DecodeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public static DecodeException ofIO(IOException e) {
        DecodeException ex = new DecodeException(e.getMessage());
        ex.setStackTrace(e.getStackTrace());
        return ex;
    }
}
