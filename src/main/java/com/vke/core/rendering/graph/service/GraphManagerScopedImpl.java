package com.vke.core.rendering.graph.service;

import com.vke.core.rendering.graph.RenderGraph;
import com.vke.core.Context;
import com.vke.utils.io.Identifier;

import java.util.List;

public class GraphManagerScopedImpl implements GraphManager {
    private final Context context;
    private final GraphManagerBaseImpl base;

    public GraphManagerScopedImpl(Context context, GraphManagerBaseImpl base) {
        this.context = context;
        this.base = base;
    }

    @Override
    public void initialize() {
        try {
            base.registerGraphs(context, context.id("/graphs"));
        } catch (Exception e) {
            context.throwException(e, "GraphManagerScopedImpl#initialize");
        }
    }

    @Override
    public void onRendererAvailable() {
        base.onRendererAvailable();
    }

    @Override
    public RenderGraph getGraph(String name) {
        return base.getGraph(context.id(name));
    }

    @Override
    public RenderGraph getGraph(Identifier name) {
        return base.getGraph(name);
    }

    @Override
    public String getId() {
        return base.getId();
    }

    @Override
    public List<String> dependencies() {
        return base.dependencies();
    }

    @Override
    public void free() {

    }
}
