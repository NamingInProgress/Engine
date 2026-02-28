package com.vke.utils.iter.helpers;

import com.vke.utils.fi.FaultySupplier;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Option<T> {
    private T value;
    private boolean some;

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

    public static <T> Option<T> useIfNotNull(@Nullable T nullable) {
        if (nullable == null) return Option.none();
        return Option.some(nullable);
    }

    public static <T> Option<T> useIfNotFaulty(FaultySupplier<T, ? extends Throwable> value) {
        try {
            return Option.some(value.get());
        } catch (Throwable _) {
            return Option.none();
        }
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

    @SuppressWarnings("unchecked")
    public <O, R> Option<R> flatMap(Function<O, R> mapper) {
        if (isNone()) return Option.none();
        T t = unwrap();
        if (t instanceof Option<?> unknown) {
            Option<O> opt = (Option<O>) unknown;
            if (opt.isNone()) return Option.none();
            O o = opt.unwrap();
            return Option.some(mapper.apply(o));
        }
        return Option.none();
    }

    @SuppressWarnings("unchecked")
    public <R> Option<R> flatten() {
        if (isNone()) return Option.none();
        T t = unwrap();
        if (t instanceof Option<?> unknown) {
            Option<R> opt = (Option<R>) unknown;
            return opt.flatten();
        }
        return Option.some((R) t);
    }

    public Option<T> take() {
        if (isNone()) return Option.none();
        T t = unwrap();
        value = null;
        some = false;
        return Option.some(t);
    }

    public Optional<T> asOptional() {
        if (isNone()) return Optional.empty();
        return Optional.ofNullable(value);
    }

    @Override
    public String toString() {
        return isSome() ? "Some(" + value + ")" : "None";
    }

    @Override
    public int hashCode() {
        return isSome() ? Objects.hashCode(value) : 0;
    }
}
