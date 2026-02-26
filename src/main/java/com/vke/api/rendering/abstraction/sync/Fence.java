package com.vke.api.rendering.abstraction.sync;

import com.vke.utils.Disposable;

public interface Fence extends Disposable {

    boolean isSignaled();

    void waitForFence();
    void reset();

    /**
     *
     * @param timeout - The timeout in nanoseconds
     * @return true - SUCCESS, false - TIMEOUT
     */
    boolean waitForFence(long timeout);

}
