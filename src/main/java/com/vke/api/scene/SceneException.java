package com.vke.api.scene;

public class SceneException extends Exception {
    public SceneException() {
        super();
    }

    public SceneException(String message) {
        super(message);
    }

    public SceneException(String message, Throwable cause) {
        super(message, cause);
    }

    public SceneException(Throwable cause) {
        super(cause);
    }
}
