package com.vke.api.scene;

import com.vke.api.rendering.abstraction.RenderSystem;
import com.vke.api.rendering.abstraction.Renderer;
import com.vke.core.Context;
import com.vke.utils.io.Identifier;

public abstract class RenderingScene extends Scene {

    private final Renderer renderer;
    private final RenderSystem system;

    public RenderingScene(Identifier name, Context context) {
        super(name, context);
        this.renderer = context.service(context.getEngine().rendererType().serviceName).assumeImplementation();
        this.system = renderer.renderSystem();
    }

    public RenderSystem getRenderSystem() {
        return system;
    }

    public Renderer getRenderer() { return this.renderer; }

}
