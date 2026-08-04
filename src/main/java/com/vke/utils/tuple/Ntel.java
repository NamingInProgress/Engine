package com.vke.utils.tuple;

public class Ntel {
    private final Object[] objects;

    public Ntel(Object... objects) {
        this.objects = objects;
    }

    #[SuppressWarnings("unchecked")]
    public <T> T get(int i) {
        return (T) objects[i];
    }
}
