package com.vke.core.rendering.graph.service;

import com.vke.core.rendering.graph.RenderGraph;
import com.vke.api.services2.PinnedService;
import com.vke.utils.io.Identifier;

public interface GraphManager extends PinnedService {
    void initialize();
    void onRendererAvailable();

    RenderGraph getGraph(String name);
    RenderGraph getGraph(Identifier name);
}
