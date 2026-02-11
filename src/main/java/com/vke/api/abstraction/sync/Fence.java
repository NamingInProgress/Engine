package com.vke.api.abstraction.sync;

import com.vke.utils.Disposable;

public interface Fence extends Disposable {

    boolean isSignaled();

    void waitForFence();
    void reset();

    boolean waitForFence(long timeout);

}
