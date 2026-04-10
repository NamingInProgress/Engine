package com.vke.core.file.obj;

public class ObjException extends Exception {
    public ObjException() {
        super();
    }

    public ObjException(String message) {
        super(message);
    }

    public ObjException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObjException(Throwable cause) {
        super(cause);
    }
}
