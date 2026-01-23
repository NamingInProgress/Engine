package com.vke.core.rendering.vulkan;

import java.util.function.BiConsumer;

public class StructureChain3<A, B, C> {
    private A a;
    private B b;
    private BiConsumer<A, B> pNextOfA;
    private BiConsumer<B, C> pNextOfB;

    public StructureChain3(BiConsumer<A, B> pNextOfA, BiConsumer<B, C> pNextOfB) {
        this.pNextOfA = pNextOfA;
        this.pNextOfB = pNextOfB;
    }

    public void add1(A a) {
        this.a = a;

    }

    public void add2(B b) {
        pNextOfA.accept(a, b);
        this.b = b;
    }

    public void add3(C c) {
        pNextOfB.accept(b, c);
    }

    public A build() {
        return a;
    }
}
