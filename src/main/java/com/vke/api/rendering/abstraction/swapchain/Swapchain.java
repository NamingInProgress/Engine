package com.vke.api.rendering.abstraction.swapchain;

import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.abstraction.enums.texture.Format;
import com.vke.api.rendering.abstraction.sync.Semaphore;
import com.vke.utils.io.Disposable;

public interface Swapchain extends Disposable {

    record Description(boolean vsync, long windowHandle) {}

    int width();
    int height();
    Format format();

    int acquireNextImage(Semaphore imageAvailable);

    void present(Semaphore renderFinished);

    void resize(int width, int height);
    void recreate();
    void destroy();

    Texture renderTarget();
    Texture depthTarget();

}
