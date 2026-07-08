package com.vke.test.module;

import com.vke.api.scene.Scene;
import com.vke.core.Context;
import com.vke.core.rendering.draw.DrawContext;
import com.vke.core.services2.ServiceManager;
import com.vke.utils.io.Identifier;

public class ModulesScene extends Scene {
    public ModulesScene(Identifier name, Context context) {
        super(name, context);
    }

    @Override
    public void onLoad() {
        System.out.println("module test");

        ServiceManager sm = context.getEngine().getServiceManager();
        sm.replaceImpl("vkr", new DummyRenderer(context.getEngine()));
    }

    @Override
    public void onDraw(DrawContext ctx) {

    }

    @Override
    public void onUnload() {

    }

    @Override
    public void free() {

    }
}
