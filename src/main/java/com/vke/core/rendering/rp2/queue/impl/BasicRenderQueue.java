package com.vke.core.rendering.rp2.queue.impl;

import com.vke.core.rendering.rp2.queue.RenderQueue;
import com.vke.core.rendering.vertexconsumer.RecyclerArrayList;
import com.vke.utils.iter.Iter;
import com.vke.utils.iter.helpers.Option;
import org.jetbrains.annotations.NotNull;

public class BasicRenderQueue<T> implements RenderQueue<T> {

    private final RecyclerArrayList<T> list = new RecyclerArrayList<>(100);

    public Iter<T> iterator() {
        return list.iter();
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public void add(T instance) {
        list.add(instance);
    }

}
