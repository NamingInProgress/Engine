package com.vke.utils.iter.helpers;

import com.vke.utils.fi.FaultyFunction;
import com.vke.utils.fi.FaultySupplier;

import java.util.Arrays;
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

    public static <T> Option<T> useIfNotNull(T nullableValue) {
        if (nullableValue == null) {
            return Option.none();
        }
        return Option.some(nullableValue);
    }

    public static <T> Option<T> useIfNotFaulty(FaultySupplier<T, ? extends Throwable> faultySupplier) {
        try {
            return Option.some(faultySupplier.get());
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

    @SuppressWarnings("unchecked")
    public T unwrapOrDefault(T... ignore) {
        if (isSome()) return value;

        Class<?> c = ignore.getClass().getComponentType();
        if (c == Byte.class) {
            return (T) (Byte) (byte) 0;
        }
        if (c == Short.class) {
            return (T) (Short) (short) 0;
        }
        if (c == Integer.class) {
            return (T) (Integer) 0;
        }
        if (c == Long.class) {
            return (T) (Long) 0L;
        }
        if (c == Float.class) {
            return (T) (Float) 0f;
        }
        if (c == Double.class) {
            return (T) (Double) 0D;
        }
        if (c == Boolean.class) {
            return (T) (Boolean) false;
        }
        if (c == Character.class) {
            return (T) (Character) '\0';
        }
        if (c == String.class) {
            return (T) "";
        }

        return null;
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
