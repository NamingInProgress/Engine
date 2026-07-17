package com.vke.api.app;

import com.vke.api.framable.Framable;
import com.vke.core.VKEngine;
import com.vke.utils.io.Disposable;

public abstract class App implements Disposable, Framable {
    public abstract void onInit(VKEngine engine);
    public abstract String getName();
}
