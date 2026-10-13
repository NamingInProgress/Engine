package com.vke.core.ui.signal;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class Signal<T> {
    private T value;
    private long version;

    public Signal(T init) {
        this.value = init;
        this.version = 0;
    }

    public Subscription subscribe() {
        return new Subscription();
    }

    public void write0(T newValue) {
        this.value = newValue;
        this.version++;
    }

    public void write1(UnaryOperator<T> func) {
        this.value = func.apply(this.value);
        this.version++;
    }

    public void write2(Supplier<T> func) {
        this.value = func.get();
        this.version++;
    }

    public void write3(Consumer<T> func) {
        func.accept(this.value);
        this.version++;
    }

    public void write4(Predicate<T> func) {
        if (func.test(this.value)) {
            this.version++;
        }
    }

    public T read() {
        return value;
    }

    public class Subscription {
        private long lastVersion;

        private Subscription() {
            this.lastVersion = version;
        }

        public T value() {
            return value;
        }

        public boolean changed() {
            return this.lastVersion != version;
        }

        public void update() {
            this.lastVersion = version;
        }
    }
}
