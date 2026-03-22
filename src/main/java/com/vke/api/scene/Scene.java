package com.vke.api.scene;

import com.vke.core.VKEngine;
import com.vke.utils.io.Disposable;
import com.vke.utils.io.Identifier;

public abstract class Scene implements Disposable {
    private final Identifier name;
    protected final VKEngine engine;

    protected Scene(Identifier name, VKEngine engine) {
        this.name = name;
        this.engine = engine;
    }

    public Identifier getName() {
        return name;
    }

    public abstract void init();
    public abstract void draw();
}
