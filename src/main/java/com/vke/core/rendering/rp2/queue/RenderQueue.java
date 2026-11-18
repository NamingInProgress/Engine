package com.vke.core.rendering.rp2.queue;

public interface RenderQueue<T> {

    void clear();
    void add(T instance);

}
