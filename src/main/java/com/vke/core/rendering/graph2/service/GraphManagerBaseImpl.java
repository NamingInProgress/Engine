package com.vke.core.rendering.graph2.service;

import com.vke.api.event.EventListener;
import com.vke.api.event.SubscribeEvent;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.schema.SchemaMismatchException;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.Renderer;
import com.vke.api.services2.ScopedServiceImpl;
import com.vke.api.window.Window;
import com.vke.api.window.WindowResizeEvent;
import com.vke.core.Context;
import com.vke.core.FileIdentifier;
import com.vke.core.Identifier;
import com.vke.core.VKEngine;
import com.vke.core.rendering.graph2.RenderGraph;
import com.vke.core.rendering.graph2.TexturePool;
import com.vke.core.rendering.graph2.parse.Graph2Parser;
import com.vke.core.rendering.graph2.parse.RenderPassReconstructor;
import com.vke.core.rendering.graph2.renderpass.RenderPass;
import com.vke.core.services2.Services;
import com.vke.utils.io.FileUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class GraphManagerBaseImpl extends ScopedServiceImpl<GraphManagerScopedImpl> implements GraphManager, EventListener {
    private final HashMap<Identifier, RenderGraph> graphs = new HashMap<>();

    private RenderSystem sys;
    private TexturePool pool;

    public GraphManagerBaseImpl(VKEngine engine) {
        super(Services.GRAPH_MANAGER, engine);
        engine.EVENT_BUS.register(this);
    }

    @Override
    public void onInitialize() {
        Renderer renderer = engine.service(Services.RENDERER);
        this.sys = renderer.renderSystem();
        this.pool = new TexturePool(sys);
    }

    @SubscribeEvent
    public void onWindowResize(WindowResizeEvent event) {
        //queueRebuild(event.window.getSize());
    }

    public void queueRebuild(Window.Size newSize) {
        for (RenderGraph g : graphs.values()) {
            g.rebuild(newSize.width(), newSize.height());
        }
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
    public void rebuildGraphs() {
        queueRebuild(engine.getWindow().getSize());
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

    private static final String RENDER_GRAPH_ROOT = "render-graph";
    private static final String RENDER_PASS_ROOT = "render-pass";

    public void registerGraphs(Context caller, FileIdentifier dir) throws SchemaMismatchException, IOException {
        RenderPassReconstructor globalReconstructor = new RenderPassReconstructor();

        for (FileIdentifier graphVclFile : dir.walkFiles()) {
            if ("vcl".equals(FileUtils.getExtensionLower(graphVclFile))) {
                ConfigDocument document = ConfigDocument.parseIdentifier(graphVclFile);
                ConfigNode root = document.getRoot();
                ConfigNode[] allNodes = root.asArray().values();
                String rootName = allNodes[0].getNodeName();
                if (RENDER_GRAPH_ROOT.equals(rootName)) {
                    RenderGraph.Def def = Graph2Parser.parseRenderGraph(caller, document, globalReconstructor);
                    //its fine to create the graph here, because it wont create renderpasses until later when load() is called
                    //so the new reconstructor stays on def and is still updated!
                    RenderGraph graph = new RenderGraph(sys, def, pool);
                    graphs.put(def.name(), graph);
                } else if (RENDER_PASS_ROOT.equals(rootName)) {
                    RenderPass.Def def = Graph2Parser.parseRenderPass(caller, document);
                    globalReconstructor.onRenderPassParsed(def);
                } else {
                    throw new IOException(String.format("Illegal root tag for file '%s': '%s'", graphVclFile, rootName));
                }
            }
        }
    }
}