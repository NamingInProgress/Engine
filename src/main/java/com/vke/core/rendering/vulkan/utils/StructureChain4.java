package com.vke.core.rendering.vulkan.utils;

import com.vke.api.utils.StructureChain;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.Struct;

public class StructureChain4<A, B extends Struct<@NotNull B>, C extends Struct<@NotNull C>, D extends Struct<@NotNull D>> implements StructureChain<A> {

    private final A a;

    public StructureChain4(A a, B b, C c, D d, PNext<A> apNext, PNext<B> bpNext, PNext<C> cpNext) {
        this.a = a;
        apNext.pNext(b.address());
        bpNext.pNext(c.address());
        cpNext.pNext(d.address());
    }

    @Override
    public A get() { return this.a; }


    public interface PNext<T> {
        T pNext(long address);
    }
}
