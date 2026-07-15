package com.vke.core.rendering.graph.service;

import com.vke.api.rendering.abstraction.rendergraph.RenderGraph;
import com.vke.api.services2.PinnedService;
import com.vke.api.services2.Service;
import com.vke.utils.io.Identifier;

public interface GraphManager extends PinnedService {
    void initialize();

    RenderGraph getGraph(String name);
    RenderGraph getGraph(Identifier name);
}
