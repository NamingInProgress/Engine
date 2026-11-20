package com.vke.api.rendering.abstraction.renderer.data;

import com.vke.utils.io.Disposable;

public interface IRenderAllocator {

    void allocate(Disposable disposable);
    void close();

}
