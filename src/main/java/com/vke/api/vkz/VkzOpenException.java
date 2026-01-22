package com.vke.api.vkz;

public class VkzOpenException extends RuntimeException {
    public VkzOpenException(String message) {
        super(message);
    }

    public VkzOpenException(Throwable cause) {
        super(cause);
    }
}
