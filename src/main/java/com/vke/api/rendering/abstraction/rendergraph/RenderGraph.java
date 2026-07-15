package com.vke.api.rendering.abstraction.rendergraph;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.rendergraph.def.RenderGraphDefinition;
import com.vke.api.rendering.abstraction.rendergraph.def.RenderPassDefinition;
import com.vke.api.window.Window;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

public class RenderGraph {

    private final RenderSystem sys;

    private final ArrayList<RenderPassInstance> passes = new ArrayList<>();
    private final HashMap<String, Texture> physicalTextures = new HashMap<>();

    private final TexturePool pool;

    private int windowWidth;
    private int windowHeight;

    public RenderGraph(RenderSystem sys, RenderGraphDefinition def, TexturePool pool) {
        this.sys = sys;
        this.pool = pool;
        for (RenderPassDefinition renderPass : def.renderPasses) {
            try {
                passes.add(new RenderPassInstance(sys, renderPass));
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                sys.getLogger().error("Failed to create render pass: " + renderPass.name());
                throw new RuntimeException(e);
            }
        }
    }

    public void updateWindowSize(Window.Size size) {
        this.windowWidth = size.width();
        this.windowHeight = size.height();
    }

    public void prepare() {
        for (RenderPassInstance pass : passes) {
            RenderPassDefinition def = pass.getDefinition();

            for (RenderPassDefinition.OutputTextureDefinition texDef : def.outputs()) {
                String globalKey = def.name() + "." + texDef.name();
                Texture tex;

                if (texDef.source() != null) {
                    tex = physicalTextures.get(texDef.source());

                    if (tex == null) {
                        throw new IllegalStateException("Render pass '" + def.name() + "' requires input '" + texDef.source() + "' but it was never created!");
                    }
                } else {
                    int width = texDef.width() == 0 ? (int) (texDef.scale() * windowWidth) : texDef.width();
                    int height = texDef.height() == 0 ? (int) (texDef.scale() * windowHeight) : texDef.height();

                    tex = pool.acquire(width, height, texDef.type(), texDef.format());
                }
                physicalTextures.put(globalKey, tex);
                pass.addOutput(texDef.name(), tex, texDef.type());
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

    public void onDraw() {
        prepare();
        CommandBuffer cmd = sys.getCurrentCommandBuffer();
        for (RenderPassInstance pass : passes) {
            pass.executor.execute(cmd, this);
        }
        endRendering();
    }

    public void endRendering() {
        for (Texture tex : physicalTextures.values()) {
            pool.release(tex);
        }
        physicalTextures.clear();
    }

}
