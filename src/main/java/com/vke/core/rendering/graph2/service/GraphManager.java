package com.vke.core.rendering.graph2.service;

import com.vke.api.services2.PinnedService;
import com.vke.core.Identifier;
import com.vke.core.rendering.graph2.RenderGraph;

public interface GraphManager extends PinnedService {
    void initialize();
    void onRendererAvailable();

    RenderGraph getGraph(String name);
    RenderGraph getGraph(Identifier name);
}
