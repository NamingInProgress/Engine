package com.vke.core.rendering.vulkan;

import java.util.function.BiConsumer;

public class StructureChain2<A, B> {
    private A a;
    private BiConsumer<A, B> pNextOfA;

    public StructureChain2(BiConsumer<A, B> pNextOfA) {
        this.pNextOfA = pNextOfA;
    }

    public void add1(A a) {
        this.a = a;

    }

    public void add2(B b) {
        pNextOfA.accept(a, b);
    }

    public A build() {
        return a;
    }
}
