package com.vke.api.scene;

import com.vke.core.Context;
import com.vke.utils.io.Disposable;
import com.vke.utils.io.Identifier;

public abstract class Scene implements Disposable {
    private final Identifier name;
    protected final Context context;

    public Scene(Identifier name, Context context) {
        this.name = name;
        this.context = context;
    }

    public Identifier getName() {
        return name;
    }

    public abstract void onLoad();

    public abstract void onUnload();
}
