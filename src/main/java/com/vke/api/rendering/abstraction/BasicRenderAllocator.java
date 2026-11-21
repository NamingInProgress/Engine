package com.vke.api.rendering.abstraction;

import com.vke.api.rendering.abstraction.renderer.data.IRenderAllocator;
import com.vke.utils.io.Disposable;

import java.util.ArrayList;

public class BasicRenderAllocator implements IRenderAllocator {

    private final ArrayList<Disposable> allocated = new ArrayList<>();

    @Override
    public void allocate(Disposable disposable) {
        allocated.add(disposable);
    }

    @Override
    public void close() {
        for (Disposable disposable : allocated) {
            disposable.free();
        }
    }
}
