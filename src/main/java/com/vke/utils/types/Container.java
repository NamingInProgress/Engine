package com.vke.utils.types;

public class Container<T> {
    private T value;

    public Container(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public void replace(T newValue) {
        this.value = newValue;
    }
}
