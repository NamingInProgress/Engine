package com.vke.core.rendering.graph;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.scene.Scene;
import com.vke.core.rendering.graph.def.RenderGraphDefinition;
import com.vke.core.rendering.graph.def.RenderPassDefinition;
import com.vke.core.window.callbacks.FramebufferCallbacks;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class RenderGraph {

    private final RenderSystem sys;

    private final ArrayList<RenderPassInstance> passes = new ArrayList<>();
    private final HashMap<String, Texture> physicalTextures = new HashMap<>();
    private final HashSet<Texture> usedTextures = new HashSet<>();

    private final TexturePool pool;

    private int windowWidth;
    private int windowHeight;

    private final GraphContext context;

    public RenderGraph(RenderSystem sys, RenderGraphDefinition def, TexturePool pool) {
        this.sys = sys;
        this.pool = pool;
        this.context = new GraphContext(sys);
        for (RenderPassDefinition renderPass : def.renderPasses) {
            try {
                passes.add(new RenderPassInstance(sys, this, renderPass));
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                sys.getLogger().error("Failed to create render pass: " + renderPass.name());
                throw new RuntimeException(e);
            }
        }

        FramebufferCallbacks.resize(this::updateWindowSize);
    }

    public void onLoad() {
        this.passes.forEach(pass -> pass.executor.onLoad());
    }

    public void updateWindowSize(int width, int height) {
        this.windowWidth = width;
        this.windowHeight = height;
    }

    public void prepare() {
        for (RenderPassInstance pass : passes) {
            pass.clear();
            RenderPassDefinition def = pass.getDefinition();

            for (RenderPassDefinition.OutputTextureDefinition texDef : def.outputs()) {
                String globalKey = def.name() + "." + texDef.name();
                Texture tex;
                if (texDef.source() != null) {
                    tex = physicalTextures.get(texDef.source());

                    if (texDef.type() == RenderPassDefinition.TextureType.RENDER_TARGET) {
                        pass.addOutput(texDef.name(), tex, false);
                        continue;
                    }

                    if (tex == null) {
                        throw new IllegalStateException("Render pass '" + def.name() + "' requires input '" + texDef.source() + "' but it was never created!");
                    }
                } else {
                    int width = texDef.width() == 0 ? (int) (texDef.scale() * windowWidth) : texDef.width();
                    int height = texDef.height() == 0 ? (int) (texDef.scale() * windowHeight) : texDef.height();

                    tex = pool.acquire(width, height, texDef.type(), texDef.format());

                    if (texDef.type() == RenderPassDefinition.TextureType.RENDER_TARGET) {
                        pass.addOutput(texDef.name(), tex, false);
                        continue;
                    }
                }
                usedTextures.add(tex);
                physicalTextures.put(globalKey, tex);
                pass.addOutput(texDef.name(), tex, texDef.source() != null);
            }
        }

        for (RenderPassInstance pass : passes) {
            RenderPassDefinition def = pass.getDefinition();

            for (RenderPassDefinition.InputTextureDefinition texDef : def.inputs()) {
                String source = texDef.source();
                Texture tex = physicalTextures.get(source);

                if (tex == null) {
                    throw new IllegalStateException("Render pass '" + def.name() + "' requires input '" + source + "' but it was never created!");
                }

                pass.addInput(texDef.localName(), tex);
            }
        }
    }

    public GraphContext getContext() {
        return context;
    }

    public void onDraw(Scene runner) {
        prepare();
        CommandBuffer cmd = sys.getCurrentCommandBuffer();
        for (RenderPassInstance pass : passes) {
            pass.execute(cmd, context);
            runner.onRenderPassFinished(pass, context);
        }
        endRendering();
    }

    public void endRendering() {
        usedTextures.forEach(pool::release);
        usedTextures.clear();
        physicalTextures.clear();
        context.clear();
    }

}
