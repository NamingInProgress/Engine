package com.vke.utils.iter.helpers;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Option<T> {
    private final T value;
    private final boolean some;

    private Option(T value, boolean some) {
        this.value = value;
        this.some = some;
    }

    public static <T> Option<T> some(T some) {
        return new Option<>(some, true);
    }

    public static <T> Option<T> none() {
        return new Option<>(null, false);
    }

    public static <T> Option<T> useIf(boolean condition, Supplier<T> value) {
        if (condition) return Option.some(value.get());
        return Option.none();
    }

    public boolean isSome() {
        return some;
    }

    public boolean isNone() {
        return !some;
    }

    public T unwrap() {
        if (isNone()) throw new IllegalStateException("Unwrapped a None Option!");
        return value;
    }

    public T expect(String message) {
        if (isNone()) throw new IllegalStateException(message);
        return value;
    }

    public T unwrapOr(T defaultValue) {
        if (isNone()) return defaultValue;
        return value;
    }

    public T unwrapOrElse(Supplier<T> defaultValue) {
        if (isNone()) return defaultValue.get();
        return value;
    }

    public void inspect(Consumer<T> inspector) {
        if (isSome()) inspector.accept(value);
    }

    public <R> Option<R> map(Function<T, R> mapper) {
        if (isNone()) return Option.none();
        return Option.some(mapper.apply(value));
    }

    public <R> R mapOr(R defaultValue, Function<T, R> mapper) {
        if (isNone()) return defaultValue;
        return mapper.apply(value);
    }

    public <R> R mapOrElse(Supplier<R> defaultValue, Function<T, R> mapper) {
        if (isNone()) return defaultValue.get();
        return mapper.apply(value);
    }

    public Optional<T> asOptional() {
        if (isNone()) return Optional.empty();
        return Optional.ofNullable(value);
    }
}
