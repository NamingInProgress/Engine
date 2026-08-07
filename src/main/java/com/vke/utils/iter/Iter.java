package com.vke.utils.iter;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.vke.utils.functionalinterface.FaultyFunction;
import com.vke.utils.tuple.Pair;
import com.vke.utils.functionalinterface.FaultyRunnable;
import com.vke.utils.iter.helpers.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Iter<T> extends Iterable<T> {
    @NotNull Option<T> next();

    @Override
    default @NotNull Iterator<T> iterator() {
        return new IteratorAdapter<>(this);
    }

    static <T> Iter<T> of(Stream<T> stream) {
        return of(stream.iterator());
    }

    static <T> Iter<T> of(Iterator<T> iterator) {
        return new OfIterator<>(iterator);
    }

    static <T> Iter<T> of(Iterable<T> iterable) {
        return new OfIterator<>(iterable.iterator());
    }


    @SafeVarargs //i have no clue what this does
    static <T> Iter<T> of(T... array) {
        return new OfArray<>(array);
    }

    default <U> Iter<U> map(Function<T, U> f) {
        return new Map<>(this, f);
    }

    default <U, E extends Throwable> Iter<U> faultyMap(FaultyFunction<T, U, E> f) {
        return new FaultyMap<>(this, f);
    }

    default Iter<T> filter(Predicate<T> p) {
        return new Filter<>(this, p);
    }

    default Iter<T> finisher(Runnable p) {
        return new Finisher<>(this, p);
    }

    default Iter<T> faultyFinisher(FaultyRunnable p) {
        return new FaultyFinisher<>(this, p);
    }

    default <U> Iter<U> filterMap(Function<T, Option<U>> f) {
        return new FilterMap<>(this, f);
    }

    default Iter<T> take(int n) {
        return new Take<>(this, n);
    }

    default Iter<T> skip(int n) {
        return new Skip<>(this, n);
    }

    default Iter<T> takeWhile(Predicate<T> p) {
        return new TakeWhile<>(this, p);
    }

    default Iter<T> skipWhile(Predicate<T> p) {
        return new SkipWhile<>(this, p);
    }

    default <U> Iter<U> flatMap(Function<T, Iter<U>> f) {
        return new FlatMap<>(this, f);
    }

    default <U> Iter<Pair<T, U>> zip(Iter<U> other) {
        return new Zip<>(this, other);
    }

    default <U> Iter<Pair<T, U>> zip(Iterable<U> other) {
        return zip(Iter.of(other));
    }

    default <U> Iter<Pair<T, U>> zip(Iterator<U> other) {
        return zip(Iter.of(other));
    }

    default <U> Iter<Pair<T, U>> zip(Stream<U> other) {
        return zip(Iter.of(other));
    }

    default <U> Iter<Pair<T, U>> zip(U... other) {
        return zip(Iter.of(other));
    }

    default Iter<T> chain(Iter<T> other) {
        return new Chain<>(this, other);
    }

    default Iter<T> chain(Iterable<T> other) {
        return chain(Iter.of(other));
    }

    default Iter<T> chain(Iterator<T> other) {
        return chain(Iter.of(other));
    }

    default Iter<T> chain(Stream<T> other) {
        return chain(Iter.of(other));
    }

    default Iter<T> chain(T... other) {
        return chain(Iter.of(other));
    }

    default Iter<ObjectCursor<T>> enumerate() {
        return new Enumerate<>(this);
    }

    default Peekable<T> peekable() {
        return new Peekable<>(this);
    }

    default Iter<T> inspect(Consumer<T> f) {
        return new Inspect<>(this, f);
    }

    default Iter<T> cycle() {
        return new Cycle<>(this);
    }

    default <U> Iter<U> cast(U... ignore) {
        return new Cast<>(this, ignore);
    }

    //term methods

    default void forEach(Consumer<? super T> f) {
        while (true) {
            Option<T> next = next();
            if (next.isNone()) return;
            f.accept(next.unwrap());
        }
    }

    default Option<T> find(Predicate<? super T> p) {
        while (true) {
            Option<T> next = next();
            if (next.isNone()) return next;
            if (p.test(next.unwrap())) return next;
        }
    }

    default boolean any(Predicate<? super T> p) {
        return find(p).isSome();
    }

    default boolean all(Predicate<? super T> p) {
        while (true) {
            Option<T> next = next();
            if (next.isNone()) return true;
            if (!p.test(next.unwrap())) return false;
        }
    }

    default <U> U fold(U init, BiFunction<U, ? super T, U> f) {
        U acc = init;
        while (true) {
            Option<T> next = next();
            if (next.isNone()) return acc;
            acc = f.apply(acc, next.unwrap());
        }
    }

    default List<T> collectToList() {
        List<T> out = new ArrayList<>();
        forEach(out::add);
        return out;
    }

    default T[] toArray(T... templateDoNotUse) {
        return collectToList().toArray(templateDoNotUse);
    }

    default Stream<T> intoStream() {
        return StreamSupport.stream(new SpliteratorAdapter<>(this), false);
    }

    default Option<T> first(Predicate<T> test) {
        return filter(test).next();
    }

    default T maxBy(Comparator<T> comparator) {
        T max = next().unwrapOrNull();
        if (max == null) return null;
        for (T e : this) {
            if (comparator.compare(e, max) > 0) {
                max = e;
            }
        }
        return max;
    }

    default int maxInt() {
        return (int) maxBy(Comparator.comparingInt(i -> (int) i));
    }

    default long maxLong() {
        return (long) maxBy(Comparator.comparingLong(i -> (long) i));
    }

    default float maxFloat() {
        return (float) maxBy(Comparator.comparingDouble(i -> (double) (float) i));
    }

    default double maxDouble() {
        return (double) maxBy(Comparator.comparingDouble(i -> (double) i));
    }

    default T minBy(Comparator<T> comparator) {
        T min = next().unwrapOrNull();
        if (min == null) return null;
        for (T e : this) {
            if (comparator.compare(e, min) < 0) {
                min = e;
            }
        }
        return min;
    }

    default int minInt() {
        return (int) minBy(Comparator.comparingInt(i -> (int) i));
    }

    default long minLong() {
        return (long) minBy(Comparator.comparingLong(i -> (long) i));
    }

    default float minFloat() {
        return (float) minBy(Comparator.comparingDouble(i -> (double) (float) i));
    }

    default double minDouble() {
        return (double) minBy(Comparator.comparingDouble(i -> (double) i));
    }
}
