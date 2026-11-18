package com.vke.core.rendering.rp.queue;

import com.vke.core.rendering.rp.RenderKeyable;

public interface RenderQueue<T extends RenderKeyable> {

    void clear();
    void add(T instance);

}
