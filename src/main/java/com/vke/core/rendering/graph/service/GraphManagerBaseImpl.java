package com.vke.core.rendering.graph.service;

import com.vke.api.parsing.config.schema.SchemaMismatchException;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.Renderer;
import com.vke.core.rendering.graph.RenderGraph;
import com.vke.core.rendering.graph.TexturePool;
import com.vke.core.rendering.graph.def.RenderGraphDefinition;
import com.vke.api.services2.ScopedServiceImpl;
import com.vke.api.window.Window;
import com.vke.core.Context;
import com.vke.core.VKEngine;
import com.vke.core.services2.Services;
import com.vke.utils.io.Identifier;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class GraphManagerBaseImpl extends ScopedServiceImpl<GraphManagerScopedImpl> implements GraphManager {
    private final HashMap<Identifier, RenderGraph> graphs = new HashMap<>();
    private RenderSystem sys;
    private TexturePool pool;

    public GraphManagerBaseImpl(VKEngine engine) {
        super(Services.GRAPH_MANAGER, engine);
    }

    @Override
    public void onInitialize() {
        Renderer renderer = engine.service(Services.RENDERER);
        this.sys = renderer.renderSystem();
        this.pool = new TexturePool(sys);
    }

    public void onWindowResize(Window.Size newSize) {
        graphs.values().forEach((g) -> g.updateWindowSize(newSize));
    }

    @Override
    public void onRendererAvailable() {
        graphs.values().forEach(RenderGraph::onLoad);
    }

    @Override
    public RenderGraph getGraph(String name) {
        return getGraph(engine.id(name));
    }

    @Override
    public RenderGraph getGraph(Identifier name) {
        var g = this.graphs.get(name);
        if (g == null) {
            engine.throwException(new IllegalStateException("Requested RenderGraph '%s' is null!".formatted(name)), "GetGraph");
        }
        return g;
    }

    @Override
    protected GraphManagerScopedImpl createScoped(Context context) {
        return new GraphManagerScopedImpl(context, this);
    }

    @Override
    public List<String> dependencies() {
        return List.of(Services.POST_PROCESS);
    }

    @Override
    public void free() {
        pool.free();
    }

    public void registerGraphs(Context caller, Identifier dir) throws SchemaMismatchException, IOException, ClassNotFoundException {
        for (Identifier graphVclFile : dir.walkFiles()) {
            RenderGraphDefinition def = new RenderGraphDefinition(caller, graphVclFile);
            RenderGraph graph = new RenderGraph(sys, def, pool);
            graphs.put(graphVclFile.strip(), graph);
        }
        onWindowResize(caller.getEngine().getWindow().getSize());
    }
}
