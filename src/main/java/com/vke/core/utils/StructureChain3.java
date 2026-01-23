package com.vke.core.utils;

import com.carrotsearch.hppc.procedures.LongProcedure;
import com.vke.api.utils.StructureChain;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.Struct;

public class StructureChain3<A, B extends Struct<@NotNull B>, C extends Struct<@NotNull C>> implements StructureChain<A> {

    private final A a;

    public StructureChain3(A a, B b, C c, LongProcedure apNext, LongProcedure bpNext) {
        this.a = a;
        apNext.apply(b.address());
        bpNext.apply(c.address());
    }

    @Override
    public A get() { return this.a; }

}
