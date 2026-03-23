package com.vke.test.scene;

import com.vke.api.scene.Scene;
import com.vke.core.Context;
import com.vke.utils.io.Identifier;

public class MainScene extends Scene {
    public MainScene(Identifier name, Context context) {
        super(name, context);
    }

    @Override
    public void onLoad() {
        System.out.println("Hello main scene!");
    }

    @Override
    public void onUnload() {
        System.out.println("Bye main scene!");
    }

    @Override
    public void free() {

    }
}
