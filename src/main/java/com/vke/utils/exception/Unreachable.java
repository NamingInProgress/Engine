package com.vke.utils.exception;

public class Unreachable extends RuntimeException {
    public Unreachable() {
        super("This path is unreachable");
    }
}
