package com.vke.core.file.gzip.deflate.exc;

public class InflatingException extends Exception {
    public InflatingException() {
        super();
    }

    public InflatingException(String message) {
        super(message);
    }

    public InflatingException(String message, Throwable cause) {
        super(message, cause);
    }

    public InflatingException(Throwable cause) {
        super(cause);
    }
}
