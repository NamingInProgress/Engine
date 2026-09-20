package com.vke.core.rendering.graph2.service;

import com.vke.api.services2.ServiceAPI;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.Identifier;
import com.vke.core.rendering.graph2.RenderGraph;

public class GraphManagerAPI extends ServiceAPI implements GraphManager {

    public GraphManagerAPI(ServiceImpl baseImpl) {
        super(baseImpl.getId(), baseImpl);
    }

    private GraphManager getImpl() {
        return (GraphManager) getImplementation();
    }

    @Override
    public void onRendererAvailable() {
        getImpl().onRendererAvailable();
    }

    @Override
    public RenderGraph getGraph(String name) {
        return getImpl().getGraph(name);
    }

    @Override
    public RenderGraph getGraph(Identifier name) {
        return getImpl().getGraph(name);
    }

    @Override
    public void rebuildGraphs() {
        getImpl().rebuildGraphs();
    }
}
