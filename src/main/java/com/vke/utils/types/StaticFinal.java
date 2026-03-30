package com.vke.utils.types;

import com.vke.utils.iter.helpers.Option;
import org.jetbrains.annotations.Nullable;

public class StaticFinal<T> {
    private Option<T> value;

    public StaticFinal() {
        value = Option.none();
    }

    public StaticFinal(T value) {
        this.value = Option.some(value);
    }

    public void trySet(T value) {
        if (this.value.isNone()) {
            this.value = Option.some(value);
        }
    }

    public Option<T> get() {
        return value;
    }

    public T getUnchecked() {
        return value.expect("Expected StaticFinal to be initialized!");
    }

    public @Nullable T getNullable(T... ignore) {
        return value.unwrapOrDefault(ignore);
    }
}
