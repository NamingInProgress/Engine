package com.vke.api.abstraction.swapchain;

import com.vke.api.abstraction.descriptors.texture.TextureFormat;
import com.vke.api.abstraction.sync.Semaphore;
import com.vke.utils.io.Disposable;

public interface Swapchain extends Disposable {

    record Description(boolean vsync, long windowHandle) {}

    int width();
    int height();
    TextureFormat format();

    int acquireNextImage(Semaphore imageAvailable);

    void present(Semaphore renderFinished);

    void resize(int width, int height);
    void recreate();
    void destroy();

}
