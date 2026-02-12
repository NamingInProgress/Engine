package com.vke.api.abstraction.swapchain;

import com.vke.api.abstraction.RenderDevice;
import com.vke.api.abstraction.data.Texture;
import com.vke.api.abstraction.descriptors.texture.TextureFormat;
import com.vke.api.abstraction.sync.Semaphore;
import com.vke.core.VKEngine;
import com.vke.utils.Disposable;

public interface Swapchain extends Disposable {

    record Description(VKEngine engine, RenderDevice device, boolean vsync, long windowHandle) {}

    int width();
    int height();
    TextureFormat format();

    int acquireNextImage(Semaphore imageAvailable);
    Texture getImage(int index);

    default Texture acquireAndGet(Semaphore imageAvailable) {
        return getImage(acquireNextImage(imageAvailable));
    }

    void present(Semaphore renderFinished);

    void resize(int width, int height);
    void recreate();
    void destroy();

}
