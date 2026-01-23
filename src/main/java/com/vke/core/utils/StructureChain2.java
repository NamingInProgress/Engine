package com.vke.core.utils;

import com.carrotsearch.hppc.procedures.LongProcedure;
import com.vke.api.utils.StructureChain;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.Struct;

public class StructureChain2<A, B extends Struct<@NotNull B>> implements StructureChain<A> {

    private final A a;

    public StructureChain2(A a, B b, LongProcedure apNext) {
        this.a = a;
        apNext.apply(b.address());
    }

    @Override
    public A get() { return this.a; }

}