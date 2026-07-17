package com.vke.core.rendering.graph.service;

import com.vke.core.rendering.graph.RenderGraph;
import com.vke.api.services2.ServiceAPI;
import com.vke.api.services2.ServiceImpl;
import com.vke.utils.io.Identifier;

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
}
