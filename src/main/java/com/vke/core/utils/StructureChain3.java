package com.vke.core.utils;

import com.carrotsearch.hppc.procedures.LongProcedure;
import com.vke.api.utils.StructureChain;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.Struct;

import java.util.function.Function;
import java.util.function.LongConsumer;

public class StructureChain3<A, B extends Struct<@NotNull B>, C extends Struct<@NotNull C>> implements StructureChain<A> {

    private final A a;

    public StructureChain3(A a, B b, C c, PNext<A> apNext, PNext<B> bpNext) {
        this.a = a;
        apNext.pNext(b.address());
        bpNext.pNext(c.address());
    }

    @Override
    public A get() { return this.a; }


    public interface PNext<T> {
        T pNext(long address);
    }
}
