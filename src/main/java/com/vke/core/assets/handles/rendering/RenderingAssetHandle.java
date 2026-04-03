package com.vke.core.assets.handles.rendering;

import com.vke.api.rendering.abstraction.RenderDevice;
import com.vke.api.rendering.abstraction.Renderer;
import com.vke.core.Context;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.assets.handles.CacheOnceAssetHandle;

import java.io.IOException;

public abstract class RenderingAssetHandle<T> extends CacheOnceAssetHandle<T> {
    protected abstract T acquire(VKEngine engine, RenderDevice renderDevice) throws IOException;

    @Override
    public T prepareCache(Context context) throws IOException {
        VKEngine engine = context.getEngine();
        EngineCreateInfo.RendererType rendererType = engine.rendererType();
        Renderer renderer = engine.service(rendererType.serviceName);
        RenderDevice device = renderer.getDevice();
        return acquire(engine, device);
    }
}
