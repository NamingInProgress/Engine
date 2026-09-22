package com.vke.api.rendering.abstraction.renderer.sync;

import com.vke.utils.io.Disposable;

public interface Fence extends Disposable {

    boolean isSignaled();

    int waitForFence();
    void reset();

    /**
     *
     * @param timeout - The timeout in nanoseconds
     * @return true - SUCCESS, false - TIMEOUT
     */
    int waitForFence(long timeout);

}
