package com.vke.core.rendering.vulkan;

import java.util.function.Consumer;

public class StructChain<A, B> {

    private final A a;

    public StructChain(A a, B b, Consumer<B> apNext) {
        this.a = a;
        apNext.accept(b);
    }

    public A get() { return this.a; }

}
